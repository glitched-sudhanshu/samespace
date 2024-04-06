package com.example.samespace.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.samespace.MyApp
import com.example.samespace.R
import com.example.samespace.models.Resource
import com.example.samespace.models.SongsList
import kotlinx.coroutines.flow.distinctUntilChanged

class SongPlayerFragment(val isTopTracks: Boolean) : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val songPointer by viewModel.songPointer.observeAsState(0)
                    val songsList by if (isTopTracks) {
                        viewModel.topTrackSongsList.observeAsState()
                    } else {
                        viewModel.songsList.observeAsState()
                    }
                    val colors by animateColorAsState(
                        targetValue =
                            if (songsList is Resource.Success<SongsList>) {
                                Color(
                                    android.graphics.Color.parseColor(
                                        songsList?.data?.data?.get(songPointer)?.accent ?: "#000000",
                                    ),
                                )
                            } else {
                                Color.Black
                            },
                        label = "bg-color",
                        animationSpec = tween(durationMillis = 200),
                    )
                    val gradient =
                        Brush.linearGradient(
                            colors =
                                listOf(
                                    colors.copy(alpha = .7f),
                                    colors.copy(alpha = .90f),
                                    colors,
                                ),
                        )
                    var pauseSong by remember { mutableStateOf(false) }
                    Scaffold { internalPadding ->
                        Column(
                            modifier =
                                Modifier
                                    .padding(internalPadding)
                                    .fillMaxSize()
                                    .background(gradient)
                                    .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceAround,
                        ) {
                            when (songsList) {
                                is Resource.Loading -> {
                                    Text(text = "Loading", color = Color.Black)
                                }

                                is Resource.Success<SongsList> -> {
                                    val songs =
                                        (songsList as Resource.Success<SongsList>).data?.data
                                    val pagerState =
                                        rememberPagerState(
                                            initialPage = songPointer,
                                            initialPageOffsetFraction = 0f,
                                            pageCount = { songsList?.data?.data?.size ?: 0 },
                                        )

                                    LaunchedEffect(key1 = pagerState, block = {
                                        snapshotFlow { pagerState.currentPage }.distinctUntilChanged()
                                            .collect {
                                                viewModel.setSongPosition(it, isTopTracks)
                                            }
                                    })

                                    LaunchedEffect(key1 = songPointer, block = {
                                        pagerState.animateScrollToPage(songPointer)
                                        val songUrl = songs?.get(songPointer)?.url
                                        (requireActivity().application as MyApp).musicService?.setSong(songUrl ?: "")
                                    })

                                    HorizontalPager(
                                        state = pagerState,
                                        key = {
                                            songs?.get(it)?.id ?: "121"
                                        },
                                        modifier = Modifier.fillMaxWidth(.75f),
                                    ) { index ->
                                        val pageOffset =
                                            (pagerState.currentPage - index) + pagerState.currentPageOffsetFraction
                                        val imageSize by animateFloatAsState(
                                            targetValue = if (pageOffset != 0.0f) 0.75f else 1f,
                                            label = "image-size",
                                            animationSpec = tween(durationMillis = 300),
                                        )
                                        AsyncImage(
                                            modifier =
                                                Modifier
                                                    .size(400.dp)
                                                    .graphicsLayer {
                                                        scaleX = imageSize
                                                        scaleY = imageSize
                                                    }
                                                    .padding(16.dp),
                                            model =
                                                ImageRequest.Builder(LocalContext.current)
                                                    .data(songs?.get(index)?.cover).build(),
                                            contentDescription = "cover-image",
                                            contentScale = ContentScale.Crop,
                                        )
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(vertical = 10.dp),
                                    ) {
                                        Text(
                                            text = songs?.get(songPointer)?.name ?: "",
                                            color = Color.White,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.W500,
                                        )
                                        Text(
                                            text = songs?.get(songPointer)?.artist ?: "",
                                            color =
                                                colorResource(
                                                    id = R.color.white50,
                                                ),
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.W400,
                                        )
                                    }
                                    Row(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(top = 20.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceAround,
                                    ) {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(R.drawable.ic_back),
                                            contentDescription = "prev-song",
                                            tint =
                                                colorResource(
                                                    id = R.color.white50,
                                                ),
                                            modifier =
                                                Modifier
                                                    .clickable {
                                                        viewModel.previousSong(isTopTracks)
                                                    }
                                                    .clip(CircleShape)
                                                    .size(50.dp),
                                        )
                                        Icon(
                                            imageVector =
                                                if (pauseSong) {
                                                    Icons.Default.PlayArrow
                                                } else {
                                                    ImageVector.vectorResource(
                                                        R.drawable.ic_pause,
                                                    )
                                                },
                                            contentDescription = "play-pause-song",
                                            tint = Color.Black,
                                            modifier =
                                                Modifier
                                                    .size(50.dp)
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        if (pauseSong) {
                                                            (requireActivity().application as MyApp).musicService?.resumeMusic()
                                                        } else {
                                                            (requireActivity().application as MyApp).musicService?.pauseMusic()
                                                        }
                                                        pauseSong = !pauseSong
                                                    }
                                                    .background(Color.White)
                                                    .padding(all = 5.dp),
                                        )

                                        Icon(
                                            imageVector = ImageVector.vectorResource(R.drawable.ic_next),
                                            contentDescription = "next-song",
                                            tint =
                                                colorResource(
                                                    id = R.color.white50,
                                                ),
                                            modifier =
                                                Modifier
                                                    .clickable {
                                                        viewModel.nextSong(isTopTracks)
                                                    }
                                                    .clip(CircleShape)
                                                    .size(50.dp),
                                        )
                                    }
                                }

                                is Resource.Error -> {
                                    Text(text = "Error", color = Color.Black)
                                }

                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}
