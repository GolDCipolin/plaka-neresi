package com.plakaneresi.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plakaneresi.app.R
import com.plakaneresi.app.plates.MatchKind
import com.plakaneresi.app.plates.Province
import com.plakaneresi.app.plates.SearchHit
import com.plakaneresi.app.ui.theme.PlakaNeresiTheme
import com.plakaneresi.app.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    viewModel: PlateViewModel = viewModel(),
) {
    var showDetails by rememberSaveable { mutableStateOf(false) }

    if (showDetails) {
        DetailsSheet(
            themeMode = themeMode,
            onThemeModeChange = onThemeModeChange,
            onDismiss = { showDetails = false },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = { DetailsAction(onClick = { showDetails = true }) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            // Scaffold does not inset its bottomBar for us. Without this the banner is
            // partly hidden behind the system navigation bar — invisible under gesture
            // navigation, badly clipped under 3-button navigation.
            AdBanner(modifier = Modifier.navigationBarsPadding())
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            PlateSearchField(
                value = viewModel.query,
                onValueChange = viewModel::onQueryChange,
                onClear = viewModel::onClear,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            if (viewModel.isSearching) {
                ResultsSection(hits = viewModel.hits)
            } else {
                BrowseSection(
                    provinces = viewModel.browseList,
                    sort = viewModel.sort,
                    onSortChange = viewModel::onSortChange,
                )
            }
        }
    }
}

@Composable
private fun ResultsSection(hits: List<SearchHit>) {
    if (hits.isEmpty()) {
        EmptyState()
        return
    }

    // Promote the top hit to a big card when we are confident it is *the* answer:
    // an exact code, or the only thing that matched at all.
    val leader = hits.first()
    val showLeaderCard = leader.kind == MatchKind.EXACT_CODE || hits.size == 1
    val rest = if (showLeaderCard) hits.drop(1) else hits

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (showLeaderCard) {
            item(key = "answer") { AnswerCard(leader.province) }
        }
        if (rest.isNotEmpty()) {
            if (showLeaderCard) {
                item(key = "others") { SectionLabel(stringResource(R.string.other_matches)) }
            }
            items(rest, key = { it.province.code }) { ProvinceRow(it.province) }
        }
    }
}

@Composable
private fun BrowseSection(
    provinces: List<Province>,
    sort: BrowseSort,
    onSortChange: (BrowseSort) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "header") {
            Column {
                SectionLabel(stringResource(R.string.browse_title))
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = sort == BrowseSort.CODE,
                        onClick = { onSortChange(BrowseSort.CODE) },
                        label = { Text(stringResource(R.string.sort_by_code)) },
                    )
                    FilterChip(
                        selected = sort == BrowseSort.NAME,
                        onClick = { onSortChange(BrowseSort.NAME) },
                        label = { Text(stringResource(R.string.sort_by_name)) },
                    )
                }
                Spacer(Modifier.height(4.dp))
            }
        }

        items(provinces, key = { it.code }) { ProvinceRow(it) }
    }
}

/** The answer to "48 neresi?" — one plate, one province name, one region. */
@Composable
private fun AnswerCard(province: Province) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PlateGraphic(code = province.plateCode, height = 84.dp, codeSize = 46.sp)
            Spacer(Modifier.height(20.dp))
            Text(
                text = province.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.region_format, province.region.displayName),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
            )
        }
    }
}

@Composable
private fun ProvinceRow(province: Province) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlateGraphic(code = province.plateCode, height = 38.dp, codeSize = 19.sp)
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text = province.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = province.region.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.no_results),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.no_results_hint),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    PlakaNeresiTheme {
        HomeScreen(themeMode = ThemeMode.SYSTEM, onThemeModeChange = {})
    }
}
