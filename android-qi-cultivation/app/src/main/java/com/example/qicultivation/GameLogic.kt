package com.example.qicultivation

import kotlin.math.max
import kotlin.random.Random

enum class Element { FIRE, WATER, EARTH, WOOD, METAL, LIGHTNING, ICE, WIND }

enum class Realm(val powerMultiplier: Float, val qiRequired: Int, val displayName: String) {
    MORTAL(1.0f, 0, "Śmiertelnik"),
    QI_CONDENSATION(1.2f, 120, "Kondensacja Qi"),
    FOUNDATION(1.5f, 360, "Fundament"),
    CORE_FORMATION(2.0f, 760, "Formacja Rdzenia"),
    NASCENT_SOUL(3.0f, 1600, "Początkująca Dusza"),
    SPIRIT_SEVERING(4.5f, 3200, "Rozcięcie Ducha"),
    VOID_RETURN(6.0f, 6200, "Powrót do Pustki")
}

enum class ItemType { WEAPON, ARMOR, PILL, MANUAL, ARTIFACT }
enum class TechniqueCategory { ATTACK, MOVEMENT, DEFENSE, SOUL, BODY, DOMAIN }
enum class BloodlineType { NONE, DRAGON, PHOENIX, TITAN, VOID, THUNDER_GOD }
enum class SpiritRootType { ORDINARY, DUAL, HEAVENLY, CHAOTIC, DIVINE }
enum class BodyType { ORDINARY, JADE_BONE, STAR_FORGED, IMMORTAL_TYRANT, CHAOS_BODY }
enum class DestinyType { COMMON, CHOSEN, HEAVEN_BLESSED, HEAVEN_REJECTED, FATE_BREAKER }
enum class WeatherType(val display: String) { CLEAR("Bezchmurne niebo"), RAIN("Deszcz duchowy"), STORM("Burza qi"), FOG("Mgliste zasłony"), AURORA("Niebianska zorza") }
enum class TimePhase(val display: String) { DAWN("Świt"), DAY("Dzień"), DUSK("Zmierzch"), NIGHT("Noc") }

enum class LocationType(val displayName: String, val danger: Int) {
    SECT_VALLEY("Dolina Sekt", 1),
    JADE_FOREST("Nefrytowy Las", 2),
    THUNDER_PEAK("Szczyt Gromu", 3),
    ABYSS_RUINS("Ruiny Otchłani", 4),
    CELESTIAL_DESERT("Niebiańska Pustynia", 5)
}

data class DynamicEvent(
    val id: String,
    val location: LocationType,
    val title: String,
    val description: String,
    val qiDelta: Int,
    val hpDelta: Int,
    val stonesDelta: Int,
    val reputationDelta: Int
)

data class Item(
    val name: String,
    val type: ItemType,
    val grade: Int,
    val stats: Map<String, Int> = emptyMap(),
    val value: Int = 100,
    val description: String = ""
)

data class Sect(
    val name: String,
    val elementAlignment: Element,
    val requiredRealm: Realm = Realm.MORTAL,
    val cultivateBonus: Int = 0,
    val reputationBonus: Int = 0,
    val techniqueBonus: Int = 0
)

data class Technique(
    val name: String,
    val category: TechniqueCategory,
    val element: Element,
    val grade: Int,
    var mastery: Int = 1,
    val description: String,
    val qiCost: Int,
    val basePower: Int
) {
    fun multiplier(): Float = 1f + (grade * 0.22f) + (mastery * 0.04f)
}

data class Bloodline(
    val type: BloodlineType,
    var purity: Int,
    val attackBonus: Float,
    val bodyBonus: Float,
    val soulBonus: Float,
    val destinyShift: Int
)

data class SpiritCore(
    val rootType: SpiritRootType,
    var spiritPower: Int,
    var soulDefense: Int,
    var comprehension: Int
)

data class BodyFoundation(
    val bodyType: BodyType,
    var toughness: Int,
    var regen: Int,
    var meridianCapacity: Int
)

data class Destiny(
    val type: DestinyType,
    var luckModifier: Int,
    var calamityResistance: Int,
    var opportunityRate: Int
)

data class DailyQuest(
    val title: String,
    val goal: Int,
    var progress: Int = 0,
    val rewardStones: Int,
    var claimed: Boolean = false
)

data class BountyMission(
    val title: String,
    val difficulty: Int,
    val rewardStones: Int,
    val flavor: String
)

open class Character(
    val name: String,
    val gender: String,
    var age: Int,
    val element: Element,
    var realm: Realm = Realm.MORTAL
) {
    var maxHp: Int = 120
    var hp: Int = maxHp
    var qi: Int = 0
    var luck: Int = 10
    var reputation: Int = 0

    var strength: Int = 12
    var vitality: Int = 12
    var agility: Int = 10
    var constitution: Int = 12

    fun takeDamage(amount: Int): Boolean {
        hp = max(0, hp - amount)
        return hp <= 0
    }

    fun restoreFullHealth() {
        hp = maxHp
    }
}

class Inventory {
    val items = mutableListOf<Item>()
    var spiritStones = 140

    fun addItem(item: Item) = items.add(item)

    fun addStarterSet() {
        addItem(Item("Miecz Błyskawic", ItemType.WEAPON, 2, mapOf("strength" to 6), 180, "Broń ucznia sztormu."))
        addItem(Item("Płaszcz Srebrnych Żył", ItemType.ARMOR, 2, mapOf("vitality" to 5, "constitution" to 3), 170))
        addItem(Item("Pigułka Pulsu Qi", ItemType.PILL, 2, value = 90, description = "+95 qi"))
        addItem(Item("Manual Burzowego Oddechu", ItemType.MANUAL, 2, mapOf("agility" to 4), 220))
    }

    fun getTotalBonus(statName: String): Int {
        return items
            .filter { it.type == ItemType.WEAPON || it.type == ItemType.ARMOR || it.type == ItemType.MANUAL || it.type == ItemType.ARTIFACT }
            .sumOf { it.stats[statName] ?: 0 }
    }

    fun useBestPill(): Int {
        val index = items.indexOfFirst { it.type == ItemType.PILL }
        if (index == -1) return 0
        val pill = items.removeAt(index)
        return 55 + pill.grade * 20
    }

    fun display(): String = "${items.size} przedmiotów | 💠 $spiritStones"
}

open class AdvancedCharacter(
    name: String,
    gender: String,
    age: Int,
    element: Element,
    realm: Realm = Realm.MORTAL
) : Character(name, gender, age, element, realm) {

    val inventory = Inventory()
    val techniques = mutableListOf<Technique>()
    var currentSect: Sect? = null

    var bloodline = Bloodline(BloodlineType.NONE, 0, 1f, 1f, 1f, 0)
    var spiritCore = SpiritCore(SpiritRootType.ORDINARY, spiritPower = 12, soulDefense = 10, comprehension = 10)
    var bodyFoundation = BodyFoundation(BodyType.ORDINARY, toughness = 12, regen = 8, meridianCapacity = 10)
    var destiny = Destiny(DestinyType.COMMON, luckModifier = 0, calamityResistance = 0, opportunityRate = 0)

    init {
        inventory.addStarterSet()
        techniques.addAll(
            listOf(
                Technique("Cięcie Gromu", TechniqueCategory.ATTACK, Element.LIGHTNING, 2, description = "Szybkie cięcie z piorunem.", qiCost = 18, basePower = 36),
                Technique("Krok Pustej Chmury", TechniqueCategory.MOVEMENT, Element.WIND, 2, description = "Unik i tempo.", qiCost = 12, basePower = 18),
                Technique("Tarcza Żelaznej Woli", TechniqueCategory.DEFENSE, Element.METAL, 2, description = "Wzmacnia obronę.", qiCost = 16, basePower = 0),
                Technique("Pieczęć Uspokojenia Ducha", TechniqueCategory.SOUL, Element.WATER, 2, description = "Wzmacnia duszę.", qiCost = 14, basePower = 20),
                Technique("Transformacja Nefrytowego Ciała", TechniqueCategory.BODY, Element.EARTH, 2, description = "Hartowanie ciała.", qiCost = 15, basePower = 24)
            )
        )
    }

    fun getEffectiveStat(statName: String): Int {
        val base = when (statName) {
            "strength" -> strength
            "vitality" -> vitality
            "agility" -> agility
            "constitution" -> constitution
            else -> 10
        }
        val bloodlineExtra = when (statName) {
            "strength" -> (bloodline.purity * bloodline.attackBonus * 0.06f).toInt()
            "constitution" -> (bloodline.purity * bloodline.bodyBonus * 0.08f).toInt()
            else -> 0
        }
        return base + inventory.getTotalBonus(statName) + bloodlineExtra
    }

    fun spiritCombatPower(): Int {
        return (spiritCore.spiritPower + spiritCore.comprehension + bloodline.soulBonus * 10).toInt()
    }

    fun destinyEventModifier(): Float {
        return 1f + (destiny.opportunityRate * 0.01f) + (destiny.luckModifier * 0.01f)
    }

    fun cultivate(minutes: Int): String {
        val baseGain = minutes + Random.nextInt(8, 20)
        val elementBonus = if (element == Element.LIGHTNING || element == Element.WIND) 5 else 2
        val sectBonus = currentSect?.cultivateBonus ?: 0
        val spiritBonus = (spiritCore.comprehension * 0.35f).toInt()
        val bodyBonus = (bodyFoundation.meridianCapacity * 0.25f).toInt()
        val destinyBonus = (destiny.opportunityRate * 0.5f).toInt()

        val total = baseGain + elementBonus + sectBonus + spiritBonus + bodyBonus + destinyBonus
        qi += total

        val nextRealm = Realm.entries.getOrNull(realm.ordinal + 1)
        if (nextRealm != null && qi >= nextRealm.qiRequired) {
            realm = nextRealm
            strength += 4
            vitality += 4
            agility += 3
            constitution += 4
            spiritCore.spiritPower += 5
            bodyFoundation.toughness += 5
            maxHp += 35
            hp = maxHp
            reputation += 15 + (currentSect?.reputationBonus ?: 0)
            return "✨ Przełom! ${name} osiąga ${realm.displayName}."
        }
        return "🧘 Kultywacja: +$total qi."
    }

    fun trainTechnique(index: Int): String {
        val tech = techniques.getOrNull(index) ?: return "❌ Brak techniki."
        val requiredQi = 10 + tech.grade * 8
        if (qi < requiredQi) return "❌ Za mało qi na trening (${requiredQi})."
        qi -= requiredQi

        val gain = Random.nextInt(1, 4) + (spiritCore.comprehension / 12) + (currentSect?.techniqueBonus ?: 0)
        tech.mastery += gain
        reputation += 1
        return "📘 ${tech.name} ulepszona (+$gain biegłości)."
    }

    fun awakenBloodline(type: BloodlineType): String {
        if (bloodline.type != BloodlineType.NONE) {
            bloodline.purity = (bloodline.purity + Random.nextInt(3, 8)).coerceAtMost(100)
            return "🩸 Wzmocniono istniejącą linię krwi (${bloodline.type}), czystość ${bloodline.purity}%"
        }

        bloodline = when (type) {
            BloodlineType.DRAGON -> Bloodline(type, 18, 1.30f, 1.2f, 1.05f, destinyShift = 4)
            BloodlineType.PHOENIX -> Bloodline(type, 16, 1.15f, 1.1f, 1.35f, destinyShift = 5)
            BloodlineType.TITAN -> Bloodline(type, 20, 1.1f, 1.45f, 1.0f, destinyShift = 2)
            BloodlineType.VOID -> Bloodline(type, 14, 1.25f, 1.15f, 1.30f, destinyShift = 6)
            BloodlineType.THUNDER_GOD -> Bloodline(type, 17, 1.40f, 1.2f, 1.2f, destinyShift = 7)
            else -> Bloodline(BloodlineType.NONE, 0, 1f, 1f, 1f, 0)
        }
        destiny.opportunityRate += bloodline.destinyShift
        luck += bloodline.destinyShift
        return "🌋 Przebudzenie linii krwi: ${bloodline.type} (${bloodline.purity}%)."
    }

    fun refineSpirit(): String {
        val cost = 35
        if (qi < cost) return "❌ Za mało qi na kultywację ducha."
        qi -= cost
        spiritCore.spiritPower += Random.nextInt(2, 6)
        spiritCore.soulDefense += Random.nextInt(1, 4)
        spiritCore.comprehension += Random.nextInt(1, 3)
        return "🕯️ Duch wzmocniony. Moc ducha ${spiritCore.spiritPower}."
    }

    fun temperBody(): String {
        val cost = 28
        if (qi < cost) return "❌ Za mało qi na hartowanie ciała."
        qi -= cost
        bodyFoundation.toughness += Random.nextInt(2, 6)
        bodyFoundation.regen += Random.nextInt(1, 4)
        bodyFoundation.meridianCapacity += Random.nextInt(1, 3)
        constitution += 1
        maxHp += 8
        hp = (hp + 18).coerceAtMost(maxHp)
        return "🛡️ Ciało zahartowane. Twardość ${bodyFoundation.toughness}."
    }

    fun setDestiny(type: DestinyType): String {
        destiny = when (type) {
            DestinyType.CHOSEN -> Destiny(type, luckModifier = 5, calamityResistance = 4, opportunityRate = 6)
            DestinyType.HEAVEN_BLESSED -> Destiny(type, luckModifier = 8, calamityResistance = 5, opportunityRate = 9)
            DestinyType.HEAVEN_REJECTED -> Destiny(type, luckModifier = -4, calamityResistance = 2, opportunityRate = -3)
            DestinyType.FATE_BREAKER -> Destiny(type, luckModifier = 3, calamityResistance = 8, opportunityRate = 4)
            else -> Destiny(DestinyType.COMMON, 0, 0, 0)
        }
        luck = (10 + destiny.luckModifier).coerceAtLeast(1)
        return "🔮 Przeznaczenie ustawione: ${destiny.type}."
    }

    fun attackWithTechnique(target: AdvancedCharacter, techIndex: Int): Pair<Int, String> {
        val technique = techniques.getOrNull(techIndex)
        if (technique == null) {
            val fallback = basicAttack(target)
            return fallback to "⚔️ Zwykły atak za $fallback obrażeń."
        }

        if (qi < technique.qiCost) {
            val fallback = basicAttack(target)
            return fallback to "⚠️ Brak qi na ${technique.name}, użyto zwykłego ataku za $fallback."
        }

        qi -= technique.qiCost
        val statCore = getEffectiveStat("strength") + getEffectiveStat("agility") / 2
        val bloodBoost = bloodline.attackBonus * (1f + bloodline.purity * 0.01f)
        val spiritBoost = 1f + (spiritCore.spiritPower * 0.01f)
        val bodyBoost = 1f + (bodyFoundation.toughness * 0.004f)
        val destinyBoost = 1f + (destiny.luckModifier * 0.015f)

        val raw = (statCore + technique.basePower) * realm.powerMultiplier * technique.multiplier()
        val critChance = (luck * 0.012f) + (technique.mastery * 0.0015f)
        val crit = Random.nextFloat() < critChance.coerceAtMost(0.55f)

        var final = raw * bloodBoost * spiritBoost * bodyBoost * destinyBoost
        if (technique.category == TechniqueCategory.SOUL) {
            final *= 1f + (spiritCore.comprehension * 0.01f)
        }
        if (technique.category == TechniqueCategory.BODY) {
            final *= 1f + (bodyFoundation.toughness * 0.006f)
        }
        if (crit) final *= 1.8f

        val damage = final.toInt().coerceAtLeast(1)
        val killed = target.takeDamage(damage)
        val log = if (killed) {
            reputation += 25
            "💥 ${technique.name}: $damage dmg, ${target.name} pokonany!"
        } else {
            "🔥 ${technique.name}: $damage dmg."
        }
        return damage to log
    }

    private fun basicAttack(target: AdvancedCharacter): Int {
        val dmg = ((getEffectiveStat("strength") * realm.powerMultiplier) * Random.nextDouble(0.85, 1.25)).toInt().coerceAtLeast(1)
        target.takeDamage(dmg)
        return dmg
    }

    fun joinSect(sect: Sect): String {
        if (realm.ordinal < sect.requiredRealm.ordinal) return "❌ Za niski poziom na sektę ${sect.name}."
        currentSect = sect
        reputation += 8
        return "🏯 Dołączono do ${sect.name}."
    }

    fun healAfterBattle() {
        val regenBonus = bodyFoundation.regen + (getEffectiveStat("vitality") / 2)
        hp = (hp + regenBonus).coerceAtMost(maxHp)
    }
}

object WorldManager {
    private const val REPEAT_CHANCE = 0.005f // 0.5%
    private val worldLog = mutableListOf<String>()
    private val usedEventIds = mutableSetOf<String>()

    fun log(event: String) {
        worldLog.add(event)
        if (worldLog.size > 60) worldLog.removeAt(0)
    }

    fun recentLogs(): List<String> = worldLog.toList()

    private fun generateDynamicEvent(location: LocationType): DynamicEvent {
        val moods = listOf("mistyczny", "krwawy", "starożytny", "chaotyczny", "eteryczny")
        val nouns = listOf("ołtarz", "wir qi", "artefakt", "zjawę", "portal", "bestię")
        val verbs = listOf("odkrywasz", "pacyfikujesz", "rozszyfrowujesz", "wchłaniasz", "powstrzymujesz")

        val title = "${location.displayName}: ${moods.random().replaceFirstChar { it.uppercase() }} ${nouns.random()}"
        val description = "Podczas eksploracji ${verbs.random()} ${nouns.random()} i stabilizujesz przepływ qi."

        val scale = location.danger
        val qiDelta = Random.nextInt(15, 45) * scale
        val hpSwing = Random.nextInt(-20, 21)
        val stones = Random.nextInt(5, 28) * scale
        val rep = Random.nextInt(-2, 7) + scale

        val id = "${location.name}-${System.nanoTime()}-${Random.nextInt(1000, 9999)}"
        return DynamicEvent(id, location, title, description, qiDelta, hpSwing, stones, rep)
    }

    private fun generateRepeatedEvent(location: LocationType): DynamicEvent? {
        val candidate = usedEventIds.filter { it.startsWith(location.name) }.randomOrNull() ?: return null
        return DynamicEvent(
            id = candidate,
            location = location,
            title = "Powrót zdarzenia z karmy miejsca",
            description = "To miejsce rezonuje z poprzednim wydarzeniem (powtórka 0.5%).",
            qiDelta = Random.nextInt(10, 30) * location.danger,
            hpDelta = Random.nextInt(-12, 15),
            stonesDelta = Random.nextInt(3, 18) * location.danger,
            reputationDelta = Random.nextInt(-1, 5)
        )
    }

    fun triggerLocationEvent(player: AdvancedCharacter, location: LocationType): String {
        val shouldRepeat = usedEventIds.isNotEmpty() && Random.nextFloat() < REPEAT_CHANCE
        val event = if (shouldRepeat) {
            generateRepeatedEvent(location) ?: generateDynamicEvent(location)
        } else {
            var generated = generateDynamicEvent(location)
            while (usedEventIds.contains(generated.id)) {
                generated = generateDynamicEvent(location)
            }
            generated
        }

        usedEventIds.add(event.id)

        player.qi += event.qiDelta
        player.hp = (player.hp + event.hpDelta).coerceIn(1, player.maxHp)
        player.inventory.spiritStones = (player.inventory.spiritStones + event.stonesDelta).coerceAtLeast(0)
        player.reputation = (player.reputation + event.reputationDelta).coerceAtLeast(0)

        val repeatMark = if (shouldRepeat) "[REPEAT 0.5%]" else "[NEW]"
        val msg = "🌍 $repeatMark ${event.title} | qi ${event.qiDelta}, hp ${event.hpDelta}, 💠 ${event.stonesDelta}, rep ${event.reputationDelta}."
        log(msg)
        log("📜 ${event.description}")
        return msg
    }
}

class GameState {
    val player = AdvancedCharacter("Li Wei", "M", 18, Element.LIGHTNING)

    val sects = listOf(
        Sect("Płonący Feniks", Element.FIRE, Realm.QI_CONDENSATION, cultivateBonus = 4, reputationBonus = 2, techniqueBonus = 1),
        Sect("Szmaragdowy Korzeń", Element.WOOD, Realm.MORTAL, cultivateBonus = 3, reputationBonus = 3, techniqueBonus = 1),
        Sect("Niebiański Grom", Element.LIGHTNING, Realm.FOUNDATION, cultivateBonus = 7, reputationBonus = 4, techniqueBonus = 3),
        Sect("Pałac Pustki", Element.WIND, Realm.CORE_FORMATION, cultivateBonus = 8, reputationBonus = 5, techniqueBonus = 4)
    )

    val shopItems = listOf(
        Item("Pigułka Skondensowanego Qi", ItemType.PILL, 2, value = 80, description = "Silna pigułka qi."),
        Item("Pancerz Żelaznej Kory", ItemType.ARMOR, 2, stats = mapOf("vitality" to 6, "constitution" to 3), value = 140),
        Item("Manual Burzowego Kroku", ItemType.MANUAL, 3, stats = mapOf("agility" to 8), value = 220),
        Item("Pieczęć Ducha", ItemType.ARTIFACT, 3, stats = mapOf("constitution" to 6, "strength" to 3), value = 260)
    )

    val dailyQuest = DailyQuest(title = "Użyj 2 technik bojowych", goal = 2, rewardStones = 120)
    val bounties = listOf(
        BountyMission("Wyczyść Szlak Duchów", difficulty = 2, rewardStones = 120, flavor = "Karawany proszą o ochronę."),
        BountyMission("Ucisz Bestię Piorunów", difficulty = 4, rewardStones = 260, flavor = "Słychać ryk z górskiego klasztoru."),
        BountyMission("Odbij Relikwię z Ruin", difficulty = 5, rewardStones = 320, flavor = "Starożytna pieczęć pęka każdej nocy.")
    )
    val notableNpcs = listOf("Mistrzyni Yun", "Stary Alchemik Qiao", "Strażnik Bram Han", "Wędrowna Wróżbitka Mei")
    val storyJournal = mutableListOf<String>()

    val locations = LocationType.entries
    var currentLocation: LocationType = LocationType.SECT_VALLEY
    var currentWeather: WeatherType = WeatherType.CLEAR
    var currentPhase: TimePhase = TimePhase.DAWN
    var playerTitle: String = "Nowicjusz Burzy"
    var companionName: String? = null

    var day = 1
    var battleCount = 0
    var cultivateCount = 0

    init {
        player.setDestiny(DestinyType.CHOSEN)
        addStory("Przysięgasz podążać ścieżką dao, niezależnie od ceny.")
    }

    fun qiProgressToNextRealm(): Float {
        val next = Realm.entries.getOrNull(player.realm.ordinal + 1) ?: return 1f
        val current = player.realm.qiRequired
        val segment = (next.qiRequired - current).coerceAtLeast(1)
        return ((player.qi - current).toFloat() / segment).coerceIn(0f, 1f)
    }

    fun cultivateAction(): String {
        day += 1
        cultivateCount += 1
        advanceWorldCycle()
        val action = player.cultivate(22)
        val event = WorldManager.triggerLocationEvent(player, currentLocation)
        updateTitle()
        WorldManager.log(action)
        addStory("Podczas ${currentPhase.display.lowercase()} w ${currentLocation.displayName.lowercase()} czujesz puls świata.")
        return "$action\n$event"
    }

    fun travelTo(location: LocationType): String {
        day += 1
        advanceWorldCycle()
        currentLocation = location
        val msg = "🧭 Podróż do: ${location.displayName} (zagrożenie ${location.danger})."
        WorldManager.log(msg)
        addStory("Wyruszasz ku ${location.displayName.lowercase()}, a wiatr niesie stare legendy.")
        return msg
    }

    fun exploreCurrentLocation(): String {
        day += 1
        advanceWorldCycle()
        val event = WorldManager.triggerLocationEvent(player, currentLocation)
        addStory("Eksploracja ${currentLocation.displayName.lowercase()} odkrywa nowe sekrety.")
        return event
    }

    fun fightAction(techniqueIndex: Int = 0): String {
        day += 1
        battleCount += 1
        advanceWorldCycle()

        val minIndex = (player.realm.ordinal - 1).coerceAtLeast(0)
        val maxIndex = (player.realm.ordinal + 1).coerceAtMost(Realm.entries.lastIndex)
        val enemyRealm = Realm.entries[Random.nextInt(minIndex, maxIndex + 1)]

        val enemy = AdvancedCharacter("Dziki Kultywator", "M", 24, Element.entries.random(), enemyRealm)
        enemy.techniques.shuffle()

        val (_, firstLog) = player.attackWithTechnique(enemy, techniqueIndex)
        dailyQuest.progress = (dailyQuest.progress + 1).coerceAtMost(dailyQuest.goal)
        var result = firstLog

        if (enemy.hp > 0) {
            val enemyTech = Random.nextInt(0, enemy.techniques.size)
            val (_, enemyLog) = enemy.attackWithTechnique(player, enemyTech)
            result += "\n$enemyLog"
        }

        if (enemy.hp <= 0) {
            val reward = Random.nextInt(45, 111)
            player.inventory.spiritStones += reward
            player.reputation += 10
            result += "\n🏆 Zwycięstwo! +$reward kamieni duchowych."
            addStory("Pokonujesz przeciwnika i umacniasz swoją legendę.")
        }

        player.healAfterBattle()
        updateTitle()
        WorldManager.log(result)
        return result
    }

    fun trainTechniqueAction(index: Int): String {
        day += 1
        advanceWorldCycle()
        val msg = player.trainTechnique(index)
        addStory("Powtarzasz ruchy aż energia techniki staje się naturalna.")
        WorldManager.log(msg)
        return msg
    }

    fun awakenBloodlineAction(type: BloodlineType): String {
        day += 1
        advanceWorldCycle()
        val msg = player.awakenBloodline(type)
        updateTitle()
        addStory("Krew rezonuje z niebem, a meridiany płoną nową mocą.")
        WorldManager.log(msg)
        return msg
    }

    fun refineSpiritAction(): String {
        day += 1
        advanceWorldCycle()
        val msg = player.refineSpirit()
        addStory("Twój duch staje się ostrzejszy od miecza.")
        WorldManager.log(msg)
        return msg
    }

    fun temperBodyAction(): String {
        day += 1
        advanceWorldCycle()
        val msg = player.temperBody()
        addStory("Każda kropla potu hartuje ciało jak stal.")
        WorldManager.log(msg)
        return msg
    }

    fun changeDestinyAction(type: DestinyType): String {
        val msg = player.setDestiny(type)
        addStory("Przeznaczenie zmienia bieg twojej ścieżki.")
        WorldManager.log(msg)
        return msg
    }

    fun restAtInnAction(): String {
        day += 1
        advanceWorldCycle()
        player.restoreFullHealth()
        val cost = 20
        player.inventory.spiritStones = (player.inventory.spiritStones - cost).coerceAtLeast(0)
        val msg = "🛏️ Odpoczynek w gospodzie: HP odnowione, koszt $cost 💠."
        addStory("W ciszy nocy regenerujesz ciało i układasz następny krok.")
        WorldManager.log(msg)
        return msg
    }

    fun talkToNpcAction(): String {
        val npc = notableNpcs.random()
        val gifts = listOf(
            "mapę ukrytej ścieżki" to 22,
            "pigułkę skupienia" to 35,
            "wskazówkę o sekretnym ołtarzu" to 18
        )
        val (gift, qiGain) = gifts.random()
        player.qi += qiGain
        companionName = if (Random.nextFloat() < 0.2f) npc else companionName
        val msg = "🗣️ Rozmowa z $npc: otrzymujesz $gift (+$qiGain qi)."
        addStory("$npc opowiada historie dawnych mistrzów i twojej możliwej przyszłości.")
        WorldManager.log(msg)
        return msg
    }

    fun acceptBountyAction(): String {
        val bounty = bounties.random()
        val successScore = player.getEffectiveStat("strength") + player.getEffectiveStat("constitution") + player.spiritCombatPower()
        val threshold = 40 + bounty.difficulty * 25
        return if (successScore >= threshold || Random.nextFloat() < 0.25f) {
            player.inventory.spiritStones += bounty.rewardStones
            player.reputation += 8 + bounty.difficulty
            val msg = "📜 Kontrakt ukończony: ${bounty.title} (+${bounty.rewardStones} 💠)."
            addStory("Mieszkańcy świętują. ${bounty.flavor}")
            WorldManager.log(msg)
            msg
        } else {
            val loss = 10 + bounty.difficulty * 3
            player.takeDamage(loss)
            val msg = "📜 Kontrakt nieudany: ${bounty.title} (−$loss HP)."
            addStory("Porażka uczy pokory i wzmacnia wolę.")
            WorldManager.log(msg)
            msg
        }
    }

    private fun advanceWorldCycle() {
        currentPhase = when (currentPhase) {
            TimePhase.DAWN -> TimePhase.DAY
            TimePhase.DAY -> TimePhase.DUSK
            TimePhase.DUSK -> TimePhase.NIGHT
            TimePhase.NIGHT -> TimePhase.DAWN
        }
        if (currentPhase == TimePhase.DAWN) {
            currentWeather = WeatherType.entries.random()
        }
    }

    private fun updateTitle() {
        playerTitle = when {
            player.realm.ordinal >= Realm.CORE_FORMATION.ordinal -> "Dziedzic Burzy i Pustki"
            player.bloodline.purity >= 60 -> "Nosiciel Niebiańskiej Krwi"
            player.reputation >= 120 -> "Sława Sekt"
            else -> "Nowicjusz Burzy"
        }
    }

    private fun addStory(entry: String) {
        storyJournal.add("Dzień $day • ${currentPhase.display} • $entry")
        if (storyJournal.size > 20) storyJournal.removeAt(0)
    }

    fun buyItem(index: Int): String {
        val item = shopItems.getOrNull(index) ?: return "❌ Brak przedmiotu."
        if (player.inventory.spiritStones < item.value) return "❌ Za mało kamieni duchowych."
        player.inventory.spiritStones -= item.value
        player.inventory.addItem(item)
        val msg = "🛒 Zakupiono ${item.name}."
        WorldManager.log(msg)
        return msg
    }

    fun usePillAction(): String {
        val gain = player.inventory.useBestPill()
        return if (gain <= 0) {
            "❌ Brak pigułek."
        } else {
            player.qi += gain
            val msg = "💊 Zużyto pigułkę: +$gain qi."
            WorldManager.log(msg)
            msg
        }
    }

    fun joinRandomSectAction(): String {
        val msg = player.joinSect(sects.random())
        WorldManager.log(msg)
        return msg
    }

    fun claimQuestReward(): String {
        if (dailyQuest.claimed) return "✅ Nagroda odebrana."
        if (dailyQuest.progress < dailyQuest.goal) return "❌ Zadanie nieukończone."
        dailyQuest.claimed = true
        player.inventory.spiritStones += dailyQuest.rewardStones
        val msg = "🎁 Nagroda questu: +${dailyQuest.rewardStones} kamieni."
        WorldManager.log(msg)
        return msg
    }
}
