#!/usr/bin/env python3
"""Terminalowy podgląd rozgrywki Kronik Kultywacji Qi.
Uruchom: python3 preview_cli.py
"""
from __future__ import annotations

from dataclasses import dataclass, field
from enum import Enum, auto
import random


class Realm(Enum):
    MORTAL = (0, "Śmiertelnik")
    QI_CONDENSATION = (120, "Kondensacja Qi")
    FOUNDATION = (360, "Fundament")
    CORE_FORMATION = (760, "Formacja Rdzenia")

    @property
    def qi_required(self) -> int:
        return self.value[0]

    @property
    def label(self) -> str:
        return self.value[1]


class BloodlineType(Enum):
    NONE = auto()
    DRAGON = auto()
    PHOENIX = auto()
    TITAN = auto()
    THUNDER_GOD = auto()


class DestinyType(Enum):
    COMMON = auto()
    CHOSEN = auto()
    HEAVEN_BLESSED = auto()
    FATE_BREAKER = auto()


class LocationType(Enum):
    SECT_VALLEY = ("Dolina Sekt", 1)
    JADE_FOREST = ("Nefrytowy Las", 2)
    THUNDER_PEAK = ("Szczyt Gromu", 3)
    ABYSS_RUINS = ("Ruiny Otchłani", 4)
    CELESTIAL_DESERT = ("Niebiańska Pustynia", 5)

    @property
    def label(self) -> str:
        return self.value[0]

    @property
    def danger(self) -> int:
        return self.value[1]


class WeatherType(Enum):
    CLEAR = "Bezchmurne niebo"
    RAIN = "Deszcz duchowy"
    STORM = "Burza qi"
    FOG = "Mgliste zasłony"


class TimePhase(Enum):
    DAWN = "Świt"
    DAY = "Dzień"
    DUSK = "Zmierzch"
    NIGHT = "Noc"


@dataclass
class Technique:
    name: str
    qi_cost: int
    base_power: int
    mastery: int = 1


@dataclass
class Character:
    name: str
    realm: Realm = Realm.MORTAL
    qi: int = 0
    hp: int = 120
    max_hp: int = 120
    strength: int = 12
    vitality: int = 12
    constitution: int = 12
    spirit_power: int = 12
    body_toughness: int = 12
    luck: int = 10
    spirit_stones: int = 120
    bloodline: BloodlineType = BloodlineType.NONE
    bloodline_purity: int = 0
    destiny: DestinyType = DestinyType.CHOSEN
    current_location: LocationType = LocationType.SECT_VALLEY
    weather: WeatherType = WeatherType.CLEAR
    phase: TimePhase = TimePhase.DAWN
    title: str = "Nowicjusz Burzy"
    generated_event_ids: set[str] = field(default_factory=set)
    story: list[str] = field(default_factory=list)
    techniques: list[Technique] = field(default_factory=lambda: [
        Technique("Cięcie Gromu", qi_cost=18, base_power=36),
        Technique("Rozbłysk Feniksa", qi_cost=22, base_power=44),
        Technique("Krok Burzy", qi_cost=14, base_power=20),
    ])

    def destiny_opportunity_bonus(self) -> int:
        return {
            DestinyType.COMMON: 0,
            DestinyType.CHOSEN: 6,
            DestinyType.HEAVEN_BLESSED: 10,
            DestinyType.FATE_BREAKER: 4,
        }[self.destiny]

    def bloodline_attack_mult(self) -> float:
        base = {
            BloodlineType.NONE: 1.0,
            BloodlineType.DRAGON: 1.25,
            BloodlineType.PHOENIX: 1.2,
            BloodlineType.TITAN: 1.15,
            BloodlineType.THUNDER_GOD: 1.35,
        }[self.bloodline]
        return base * (1 + self.bloodline_purity / 100)

    def cultivate(self) -> str:
        self.advance_cycle()
        gain = random.randint(22, 40) + self.destiny_opportunity_bonus() + self.constitution // 4
        self.qi += gain
        event_msg = self.location_event()

        realms = list(Realm)
        idx = realms.index(self.realm)
        if idx + 1 < len(realms) and self.qi >= realms[idx + 1].qi_required:
            self.realm = realms[idx + 1]
            self.strength += 4
            self.vitality += 4
            self.constitution += 3
            self.max_hp += 30
            self.hp = self.max_hp
            self.update_title()
            self.add_story("Przełamujesz barierę krainy.")
            return f"✨ Przełom do: {self.realm.label}! (+{gain} qi)\n{event_msg}"
        self.add_story("Medytacja wzmacnia rdzeń qi.")
        return f"🧘 Kultywacja udana: +{gain} qi\n{event_msg}"

    def train_technique(self, idx: int) -> str:
        if idx < 0 or idx >= len(self.techniques):
            return "❌ Nie ma takiej techniki"
        t = self.techniques[idx]
        cost = 10 + t.qi_cost // 2
        if self.qi < cost:
            return f"❌ Za mało qi ({self.qi}/{cost})"
        self.qi -= cost
        gain = random.randint(1, 4) + self.spirit_power // 15
        t.mastery += gain
        return f"📘 {t.name} +{gain} biegłości"

    def awaken_bloodline(self, typ: BloodlineType) -> str:
        if self.bloodline == BloodlineType.NONE:
            self.bloodline = typ
            self.bloodline_purity = random.randint(12, 22)
            return f"🩸 Przebudzenie linii krwi {typ.name}, czystość {self.bloodline_purity}%"
        self.bloodline_purity = min(100, self.bloodline_purity + random.randint(4, 10))
        return f"🩸 Linia krwi wzmocniona do {self.bloodline_purity}%"

    def refine_spirit(self) -> str:
        if self.qi < 30:
            return "❌ Za mało qi na rozwój ducha"
        self.qi -= 30
        self.spirit_power += random.randint(2, 5)
        return f"🕯️ Moc ducha wzrosła do {self.spirit_power}"

    def temper_body(self) -> str:
        if self.qi < 26:
            return "❌ Za mało qi na hartowanie ciała"
        self.qi -= 26
        self.body_toughness += random.randint(2, 5)
        self.constitution += 1
        self.max_hp += 8
        self.hp = min(self.max_hp, self.hp + 12)
        return f"🛡️ Ciało zahartowane (twardość: {self.body_toughness})"

    def fight(self, technique_index: int = 0) -> str:
        enemy_hp = random.randint(70, 170)
        tech = self.techniques[technique_index % len(self.techniques)]

        if self.qi >= tech.qi_cost:
            self.qi -= tech.qi_cost
            dmg = int((self.strength + tech.base_power + tech.mastery * 2) * self.bloodline_attack_mult())
        else:
            dmg = int(self.strength * 1.2)

        crit = random.random() < min(0.55, (self.luck + self.destiny_opportunity_bonus()) / 100)
        if crit:
            dmg = int(dmg * 1.8)

        enemy_hp -= dmg
        if enemy_hp <= 0:
            loot = random.randint(30, 100)
            self.spirit_stones += loot
            return f"⚔️ Zwycięstwo! dmg={dmg}, łup +{loot}💠"

        retaliation = random.randint(8, 24)
        mitigation = self.body_toughness // 6 + self.constitution // 5
        real = max(1, retaliation - mitigation)
        self.hp = max(0, self.hp - real)
        return f"⚔️ Zadajesz {dmg}, wróg kontruje za {real}. HP: {self.hp}/{self.max_hp}"

    def travel(self, location: LocationType) -> str:
        self.advance_cycle()
        self.current_location = location
        self.add_story(f"Wyruszasz ku {location.label.lower()}.")
        return f"🧭 Podróż do {location.label} (zagrożenie {location.danger})"

    def location_event(self) -> str:
        repeat_chance = 0.005  # 0.5%
        should_repeat = self.generated_event_ids and random.random() < repeat_chance

        if should_repeat:
            event_id = random.choice(list(self.generated_event_ids))
            event_type = "REPEAT 0.5%"
        else:
            event_id = f"{self.current_location.name}-{random.randint(10_000, 99_999)}"
            while event_id in self.generated_event_ids:
                event_id = f"{self.current_location.name}-{random.randint(10_000, 99_999)}"
            self.generated_event_ids.add(event_id)
            event_type = "NEW"

        scale = self.current_location.danger
        qi = random.randint(12, 40) * scale
        hp = random.randint(-18, 12)
        stones = random.randint(5, 20) * scale
        rep = random.randint(-1, 5)

        self.qi += qi
        self.hp = min(self.max_hp, max(1, self.hp + hp))
        self.spirit_stones = max(0, self.spirit_stones + stones)
        self.add_story(f"Doświadczasz wydarzenia w lokacji {self.current_location.label}.")
        return f"🌍 [{event_type}] {self.current_location.label}: qi {qi}, hp {hp}, 💠 {stones}, rep {rep}"

    def rest(self) -> str:
        self.advance_cycle()
        self.hp = self.max_hp
        self.spirit_stones = max(0, self.spirit_stones - 20)
        self.add_story("Regenerujesz ciało w gospodzie.")
        return "🛏️ Odpoczynek zakończony. HP pełne."

    def talk_npc(self) -> str:
        npc = random.choice(["Mistrzyni Yun", "Qiao Alchemik", "Wróżbitka Mei", "Strażnik Han"])
        gain = random.randint(12, 35)
        self.qi += gain
        self.add_story(f"Rozmowa z {npc} poszerza twoje horyzonty.")
        return f"🗣️ Rozmowa z {npc}: +{gain} qi."

    def bounty(self) -> str:
        score = self.strength + self.constitution + self.spirit_power
        if score + random.randint(0, 40) > 60:
            reward = random.randint(90, 260)
            self.spirit_stones += reward
            self.add_story("Kontrakt zakończony sukcesem.")
            self.update_title()
            return f"📜 Kontrakt wykonany. +{reward} 💠"
        dmg = random.randint(8, 25)
        self.hp = max(1, self.hp - dmg)
        self.add_story("Kontrakt nieudany, ale zdobywasz doświadczenie.")
        return f"📜 Kontrakt nieudany. -{dmg} HP"

    def advance_cycle(self) -> None:
        self.phase = {
            TimePhase.DAWN: TimePhase.DAY,
            TimePhase.DAY: TimePhase.DUSK,
            TimePhase.DUSK: TimePhase.NIGHT,
            TimePhase.NIGHT: TimePhase.DAWN,
        }[self.phase]
        if self.phase == TimePhase.DAWN:
            self.weather = random.choice(list(WeatherType))

    def update_title(self) -> None:
        if self.realm in {Realm.CORE_FORMATION}:
            self.title = "Dziedzic Burzy"
        elif self.bloodline_purity >= 60:
            self.title = "Nosiciel Niebiańskiej Krwi"
        else:
            self.title = "Nowicjusz Burzy"

    def add_story(self, text: str) -> None:
        self.story.append(f"{self.phase.value}: {text}")
        if len(self.story) > 8:
            self.story.pop(0)


def print_state(c: Character) -> None:
    print("\n==== STATUS ====")
    print(f"{c.name} | {c.realm.label}")
    print(f"HP: {c.hp}/{c.max_hp} | Qi: {c.qi} | 💠 {c.spirit_stones}")
    print(f"Siła: {c.strength} | Konstytucja: {c.constitution} | Duch: {c.spirit_power} | Ciało: {c.body_toughness}")
    print(f"Linia krwi: {c.bloodline.name} ({c.bloodline_purity}%) | Przeznaczenie: {c.destiny.name}")
    print(f"Tytuł: {c.title} | Pogoda: {c.weather.value} | Faza: {c.phase.value}")
    print(f"Lokacja: {c.current_location.label} | eventy wygenerowane: {len(c.generated_event_ids)}")
    print("Techniki:")
    for i, t in enumerate(c.techniques):
        print(f"  [{i}] {t.name} (moc {t.base_power}, koszt {t.qi_cost}, mastery {t.mastery})")


def main() -> None:
    c = Character(name="Li Wei")
    print("=== Kroniki Kultywacji Qi: terminal preview ===")

    while True:
        print_state(c)
        print("\nAkcje: [1]Kultywuj [2]Walcz [3]Trenuj technikę [4]Duch [5]Ciało [6]Linia krwi [7]Przeznaczenie [8]Eksploruj [9]Podróż [10]NPC [11]Odpocznij [12]Kontrakt [q]Wyjście")
        choice = input("> ").strip().lower()

        if choice == "q":
            print("Do zobaczenia, kultywatorze.")
            return
        if choice == "1":
            print(c.cultivate())
        elif choice == "2":
            idx = int(input("Index techniki: ") or "0")
            print(c.fight(idx))
        elif choice == "3":
            idx = int(input("Index techniki: ") or "0")
            print(c.train_technique(idx))
        elif choice == "4":
            print(c.refine_spirit())
        elif choice == "5":
            print(c.temper_body())
        elif choice == "6":
            print("Wybierz: dragon/phoenix/titan/thunder")
            raw = input("> ").strip().lower()
            mapping = {
                "dragon": BloodlineType.DRAGON,
                "phoenix": BloodlineType.PHOENIX,
                "titan": BloodlineType.TITAN,
                "thunder": BloodlineType.THUNDER_GOD,
            }
            print(c.awaken_bloodline(mapping.get(raw, BloodlineType.DRAGON)))
        elif choice == "7":
            print("Wybierz: common/chosen/blessed/breaker")
            raw = input("> ").strip().lower()
            mapping = {
                "common": DestinyType.COMMON,
                "chosen": DestinyType.CHOSEN,
                "blessed": DestinyType.HEAVEN_BLESSED,
                "breaker": DestinyType.FATE_BREAKER,
            }
            c.destiny = mapping.get(raw, DestinyType.CHOSEN)
            print(f"🔮 Przeznaczenie: {c.destiny.name}")
        elif choice == "8":
            print(c.location_event())
        elif choice == "9":
            print("Wybierz lokację: valley/forest/peak/ruins/desert")
            raw = input("> ").strip().lower()
            mapping = {
                "valley": LocationType.SECT_VALLEY,
                "forest": LocationType.JADE_FOREST,
                "peak": LocationType.THUNDER_PEAK,
                "ruins": LocationType.ABYSS_RUINS,
                "desert": LocationType.CELESTIAL_DESERT,
            }
            print(c.travel(mapping.get(raw, LocationType.SECT_VALLEY)))
        elif choice == "10":
            print(c.talk_npc())
        elif choice == "11":
            print(c.rest())
        elif choice == "12":
            print(c.bounty())
        else:
            print("Nieznana komenda.")
        if c.story:
            print(f"📘 Kronika: {c.story[-1]}")


if __name__ == "__main__":
    random.seed()
    main()
