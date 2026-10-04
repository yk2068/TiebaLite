package com.huanchengfly.tieba.post.ui.page.settings.visibility

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.dataStore
import com.huanchengfly.tieba.post.ui.common.prefs.PrefsScreen
import com.huanchengfly.tieba.post.ui.common.prefs.widgets.SwitchPref
import com.huanchengfly.tieba.post.ui.page.settings.LeadingIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.AvatarIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.BackNavigationIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.MyScaffold
import com.huanchengfly.tieba.post.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.ui.widgets.compose.TitleCentredToolbar
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterialApi::class)
@Destination
@Composable
fun VisibilitySettingsPage(
    navigator: DestinationsNavigator,
) {
    val context = LocalContext.current
    val bottomNavigationGroupTitle = stringResource(id = R.string.group_bottom_navigation)
    val exploreTabsGroupTitle = stringResource(id = R.string.group_explore_tabs)
    MyScaffold(
        backgroundColor = Color.Transparent,
        topBar = {
            TitleCentredToolbar(
                title = {
                    Text(
                        text = stringResource(id = R.string.title_visibility_settings),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.h6
                    )
                },
                navigationIcon = {
                    BackNavigationIcon(onBackPressed = { navigator.navigateUp() })
                }
            )
        },
    ) { paddingValues ->
        PrefsScreen(
            dataStore = context.dataStore,
            dividerThickness = 0.dp,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            prefsGroup(title = bottomNavigationGroupTitle) {
                prefsItem {
                    SwitchPref(
                        key = "hideNavHome",
                        title = stringResource(id = R.string.title_hide_nav_home),
                        defaultChecked = false,
                        leadingIcon = {
                            LeadingIcon {
                                AvatarIcon(
                                    icon = Icons.Outlined.Home,
                                    size = Sizes.Small,
                                    contentDescription = null,
                                )
                            }
                        },
                    )
                }
                prefsItem {
                    SwitchPref(
                        key = "hideExplore",
                        title = stringResource(id = R.string.title_hide_explore),
                        defaultChecked = false,
                        leadingIcon = {
                            LeadingIcon {
                                AvatarIcon(
                                    icon = Icons.Outlined.Explore,
                                    size = Sizes.Small,
                                    contentDescription = null,
                                )
                            }
                        },
                    )
                }
                prefsItem {
                    SwitchPref(
                        key = "hideNavMessage",
                        title = stringResource(id = R.string.title_hide_nav_message),
                        defaultChecked = false,
                        leadingIcon = {
                            LeadingIcon {
                                AvatarIcon(
                                    icon = Icons.Outlined.Notifications,
                                    size = Sizes.Small,
                                    contentDescription = null,
                                )
                            }
                        },
                    )
                }
                prefsItem {
                    SwitchPref(
                        key = "hideNavUser",
                        title = stringResource(id = R.string.title_hide_nav_user),
                        defaultChecked = false,
                        leadingIcon = {
                            LeadingIcon {
                                AvatarIcon(
                                    icon = Icons.Outlined.Person,
                                    size = Sizes.Small,
                                    contentDescription = null,
                                )
                            }
                        },
                    )
                }
            }
            prefsGroup(title = exploreTabsGroupTitle) {
                prefsItem {
                    SwitchPref(
                        key = "hideConcernTab",
                        title = stringResource(id = R.string.title_hide_concern_tab),
                        defaultChecked = false,
                        leadingIcon = {
                            LeadingIcon {
                                AvatarIcon(
                                    icon = Icons.Outlined.Groups,
                                    size = Sizes.Small,
                                    contentDescription = null,
                                )
                            }
                        },
                    )
                }
                prefsItem {
                    SwitchPref(
                        key = "hideHotTab",
                        title = stringResource(id = R.string.title_hide_hot_tab),
                        defaultChecked = false,
                        leadingIcon = {
                            LeadingIcon {
                                AvatarIcon(
                                    icon = Icons.Outlined.LocalFireDepartment,
                                    size = Sizes.Small,
                                    contentDescription = null,
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}
