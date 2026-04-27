package com.example.qicultivation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) { QiCultivationGameScreen() }
            }
        }
    }
}

@Composable
fun QiCultivationGameScreen() {
    val game = remember { GameState() }
    var status by remember { mutableStateOf("Droga dao stoi przed tobą.") }
    var tick by remember { mutableIntStateOf(0) }
    var selectedTechnique by remember { mutableIntStateOf(0) }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun notify(msg: String) {
        status = msg
        tick++
        scope.launch { snackbar.showSnackbar(msg) }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .background(Brush.verticalGradient(listOf(Color(0xFF0A0F1F), Color(0xFF1B2E4B))))
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HeaderCard(game = game, selectedTechnique = selectedTechnique)
            ProgressAndPowerCard(game = game, tick = tick)

            TechniqueControlCard(
                game = game,
                selectedTechnique = selectedTechnique,
                onSelectTechnique = { selectedTechnique = it },
                onTrain = { notify(game.trainTechniqueAction(selectedTechnique)) },
                onFight = { notify(game.fightAction(selectedTechnique)) }
            )

            CultivationSystemsCard(
                onCultivate = { notify(game.cultivateAction()) },
                onSpirit = { notify(game.refineSpiritAction()) },
                onBody = { notify(game.temperBodyAction()) },
                onUsePill = { notify(game.usePillAction()) }
            )

            LocationRpgCard(
                game = game,
                onExplore = { notify(game.exploreCurrentLocation()) },
                onTravel = { location -> notify(game.travelTo(location)) }
            )

            ImmersionCard(
                game = game,
                onTalk = { notify(game.talkToNpcAction()) },
                onRest = { notify(game.restAtInnAction()) },
                onBounty = { notify(game.acceptBountyAction()) }
            )

            BloodlineDestinyCard(
                onBloodline = { notify(game.awakenBloodlineAction(it)) },
                onDestiny = { notify(game.changeDestinyAction(it)) }
            )

            SectAndShopCard(
                game = game,
                onJoinSect = { notify(game.joinRandomSectAction()) },
                onBuy = { notify(game.buyItem(it)) }
            )

            QuestAndLogCard(
                game = game,
                status = status,
                tick = tick,
                onClaim = { notify(game.claimQuestReward()) }
            )
        }
    }
}

@Composable
private fun HeaderCard(game: GameState, selectedTechnique: Int) {
    val p = game.player
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xCC101B33)), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("⚡ Kroniki Kultywacji Qi", color = Color.White, style = MaterialTheme.typography.headlineSmall)
            Text("${p.name} • ${p.realm.displayName} • Dzień ${game.day}", color = Color(0xFFBFDBFE))
            Text("🏷️ Tytuł: ${game.playerTitle}", color = Color(0xFFFDE68A))
            Text("📍 Lokacja: ${game.currentLocation.displayName}", color = Color(0xFF86EFAC))
            Text("🌦️ ${game.currentWeather.display} • ⏳ ${game.currentPhase.display}", color = Color(0xFFBAE6FD))
            Text("🤝 Towarzysz: ${game.companionName ?: "Brak"}", color = Color(0xFFD8B4FE))
            Text("Technika bojowa: ${p.techniques.getOrNull(selectedTechnique)?.name ?: "Brak"}", color = Color(0xFF93C5FD))
            Text("HP ${p.hp}/${p.maxHp} | Qi ${p.qi} | Reputacja ${p.reputation}", color = Color(0xFFDDEAFE))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProgressAndPowerCard(game: GameState, tick: Int) {
    val p = game.player
    val progress = game.qiProgressToNextRealm()

    Card(colors = CardDefaults.cardColors(containerColor = Color(0xCC0F172A)), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("📈 Rozwój", color = Color.White, fontWeight = FontWeight.SemiBold)
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            Text("Postęp krainy: ${(progress * 100).toInt()}%", color = Color(0xFF93C5FD))

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text("🩸 Linia: ${p.bloodline.type} ${p.bloodline.purity}%") })
                AssistChip(onClick = {}, label = { Text("🕯️ Duch: ${p.spiritCore.spiritPower}") })
                AssistChip(onClick = {}, label = { Text("🛡️ Ciało: ${p.bodyFoundation.toughness}") })
                AssistChip(onClick = {}, label = { Text("🔮 Przeznaczenie: ${p.destiny.type}") })
                AssistChip(onClick = {}, label = { Text("💠 ${p.inventory.spiritStones}") })
                AssistChip(onClick = {}, label = { Text("⚔️ ${p.getEffectiveStat("strength")}") })
                AssistChip(onClick = {}, label = { Text("🧬 Konstytucja ${p.getEffectiveStat("constitution")}") })
            }
        }
    }
}

@Composable
private fun TechniqueControlCard(
    game: GameState,
    selectedTechnique: Int,
    onSelectTechnique: (Int) -> Unit,
    onTrain: () -> Unit,
    onFight: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xCC1E293B)), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("🗡️ System Technik i Ataków", color = Color.White, fontWeight = FontWeight.SemiBold)
            game.player.techniques.forEachIndexed { index, tech ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${tech.name} [${tech.category}]", color = Color(0xFFE2E8F0))
                        Text("Biegłość ${tech.mastery} | koszt ${tech.qiCost} qi | moc ${tech.basePower}", color = Color(0xFF93C5FD))
                    }
                    OutlinedButton(onClick = { onSelectTechnique(index) }) {
                        Text(if (selectedTechnique == index) "Wybrana" else "Wybierz")
                    }
                }
                Divider(color = Color(0xFF334155))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onTrain) { Text("Trenuj technikę") }
                Button(onClick = onFight) { Text("Walka techniką") }
            }
        }
    }
}

@Composable
private fun CultivationSystemsCard(
    onCultivate: () -> Unit,
    onSpirit: () -> Unit,
    onBody: () -> Unit,
    onUsePill: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xCC172554)), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("🧘 Kultywacja: qi • duch • ciało", color = Color.White, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onCultivate) { Text("Kultywuj Qi") }
                OutlinedButton(onClick = onSpirit) { Text("Wzmocnij ducha") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onBody) { Text("Hartuj ciało") }
                OutlinedButton(onClick = onUsePill) { Text("Użyj pigułki") }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LocationRpgCard(
    game: GameState,
    onExplore: () -> Unit,
    onTravel: (LocationType) -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xCC3F1D0A)), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("🗺️ RPG Lokacje i Auto-Zdarzenia", color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(
                "Aktualna lokacja: ${game.currentLocation.displayName} (zagrożenie ${game.currentLocation.danger})",
                color = Color(0xFFFED7AA)
            )
            Text("Wydarzenia są automatycznie generowane; szansa powtórki = 0.5%.", color = Color(0xFFFFEDD5))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onExplore) { Text("Eksploruj") }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                game.locations.forEach { location ->
                    OutlinedButton(onClick = { onTravel(location) }) {
                        Text(location.displayName)
                    }
                }
            }
        }
    }
}

@Composable
private fun ImmersionCard(
    game: GameState,
    onTalk: () -> Unit,
    onRest: () -> Unit,
    onBounty: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xCC1F2937)), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("🎭 Immersja i życie świata", color = Color.White, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onTalk) { Text("Rozmawiaj z NPC") }
                OutlinedButton(onClick = onRest) { Text("Odpocznij") }
                OutlinedButton(onClick = onBounty) { Text("Weź kontrakt") }
            }
            Text("Ostatnie wpisy kroniki:", color = Color(0xFFBFDBFE))
            game.storyJournal.takeLast(3).reversed().forEach {
                Text("• $it", color = Color(0xFFE5E7EB))
            }
        }
    }
}

@Composable
private fun BloodlineDestinyCard(
    onBloodline: (BloodlineType) -> Unit,
    onDestiny: (DestinyType) -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xCC312E81)), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("🩸 Linia krwi i 🔮 Przeznaczenie", color = Color.White, fontWeight = FontWeight.SemiBold)
            Text("Linie krwi mają ogromny wpływ na atak/ciało/duszę. Przeznaczenie wzmacnia lub osłabia okazje i karmę.", color = Color(0xFFE0E7FF))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BloodlineType.entries.filter { it != BloodlineType.NONE }.forEach { type ->
                    OutlinedButton(onClick = { onBloodline(type) }) { Text(type.name) }
                }
            }
            Spacer(Modifier.height(4.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DestinyType.entries.forEach { type ->
                    OutlinedButton(onClick = { onDestiny(type) }) { Text(type.name) }
                }
            }
        }
    }
}

@Composable
private fun SectAndShopCard(game: GameState, onJoinSect: () -> Unit, onBuy: (Int) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xCC0B3B2E)), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("🏯 Sekty i 🛒 sklep", color = Color.White, fontWeight = FontWeight.SemiBold)
            Row {
                Button(onClick = onJoinSect) { Text("Dołącz losowo do sekty") }
            }
            game.shopItems.forEachIndexed { idx, item ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, color = Color(0xFFE2E8F0))
                        Text("${item.value} 💠 | ${item.description.ifBlank { "Ranga ${item.grade}" }}", color = Color(0xFFA7F3D0))
                    }
                    Button(onClick = { onBuy(idx) }) { Text("Kup") }
                }
                Divider(color = Color(0xFF334155))
            }
        }
    }
}

@Composable
private fun QuestAndLogCard(game: GameState, status: String, tick: Int, onClaim: () -> Unit) {
    val q = game.dailyQuest
    val progress = (q.progress.toFloat() / q.goal).coerceIn(0f, 1f)

    Card(colors = CardDefaults.cardColors(containerColor = Color(0xCC111827)), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("📜 Quest dnia", color = Color.White)
            Text("${q.title}: ${q.progress}/${q.goal}", color = Color(0xFFBFDBFE))
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            Row {
                Button(onClick = onClaim, enabled = !q.claimed) { Text(if (q.claimed) "Odebrano" else "Odbierz") }
                Spacer(Modifier.width(8.dp))
                Text("Nagroda: ${q.rewardStones} 💠", color = Color(0xFF93C5FD), modifier = Modifier.padding(top = 8.dp))
            }

            Divider(color = Color(0xFF334155))
            Text("🧭 Status", color = Color.White)
            Text(status, color = Color(0xFFE2E8F0))
            Text("📖 Dziennik", color = Color.White)

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Color(0x331E293B), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                items(WorldManager.recentLogs().reversed(), key = { "$tick-$it" }) {
                    Text("• $it", color = Color(0xFFE2E8F0), modifier = Modifier.padding(vertical = 2.dp))
                }
            }
        }
    }
}
