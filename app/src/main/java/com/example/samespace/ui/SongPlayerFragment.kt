package com.example.samespace.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.activityViewModels
import androidx.media3.common.MediaItem
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.samespace.MyApp
import com.example.samespace.R
import com.example.samespace.models.Resource
import com.example.samespace.models.SongsList
import com.example.samespace.shimmerBrush
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged

class SongPlayerFragment(val isTopTracks: Boolean, private var fromBottom: Boolean) :
    BottomSheetDialogFragment() {
    private val viewModel: MainViewModel by activityViewModels()

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
                    Scaffold { internalPadding ->
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
                                            songsList?.data?.data?.get(songPointer)?.accent
                                                ?: "#000000",
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
                        val isSongPlaying by viewModel.isPlaying.observeAsState(true)
                        Column(
                            modifier =
                                Modifier
                                    .padding(internalPadding)
                                    .fillMaxSize()
                                    .background(gradient),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceAround,
                        ) {
                            when (songsList) {
                                is Resource.Loading -> {
                                    SongPlayerShimmerView()
                                }

                                is Resource.Success<SongsList> -> {
                                    Box {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            AsyncImage(
                                                modifier =
                                                    Modifier
                                                        .fillMaxSize()
                                                        .blur(radius = 12.dp)
                                                        .padding(16.dp),
                                                model =
                                                    ImageRequest.Builder(LocalContext.current)
                                                        .data(
                                                            "https://cms.samespace.com/assets/${
                                                                songsList?.data?.data?.get(
                                                                    songPointer,
                                                                )?.cover
                                                            }",
                                                        ).build(),
                                                placeholder = painterResource(id = R.drawable.bg_gradient),
                                                contentDescription = "cover-image",
                                                contentScale = ContentScale.Crop,
                                            )
                                            Box(
                                                modifier =
                                                    Modifier.fillMaxSize()
                                                        .background(color = Color.Black.copy(alpha = .4f)),
                                            )
                                        }
                                        Column(
                                            modifier =
                                                Modifier.fillMaxSize()
                                                    .padding(vertical = 40.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.SpaceAround,
                                        ) {
                                            SongPlayerScreen(
                                                songPointer = songPointer,
                                                isSongPlaying = isSongPlaying,
                                                songsList = songsList as Resource.Success<SongsList>,
                                            )
                                        }
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

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet = d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    @Composable
    fun SongPlayerShimmerView() {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(400.dp)
                        .background(shimmerBrush())
                        .clip(RoundedCornerShape(10)),
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 10.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .width(100.dp)
                            .height(40.dp)
                            .background(shimmerBrush())
                            .clip(RoundedCornerShape(5)),
                )
                Box(
                    modifier =
                        Modifier
                            .width(70.dp)
                            .height(35.dp)
                            .background(shimmerBrush())
                            .clip(RoundedCornerShape(5)),
                )
            }
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(.95f)
                        .height(2.dp)
                        .background(shimmerBrush())
                        .clip(
                            RoundedCornerShape(50),
                        ),
            )

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
                            .clip(CircleShape)
                            .size(50.dp),
                )
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "pause-song",
                    tint = Color.Black,
                    modifier =
                        Modifier
                            .size(50.dp)
                            .clip(CircleShape)
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
                            .clip(CircleShape)
                            .size(50.dp),
                )
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun SongPlayerScreen(
        songPointer: Int,
        isSongPlaying: Boolean,
        songsList: Resource.Success<SongsList>,
        modifier: Modifier = Modifier,
    ) {
        val songs =
            songsList.data?.data
        val pagerState =
            rememberPagerState(
                initialPage = songPointer,
                initialPageOffsetFraction = 0f,
                pageCount = { songsList.data?.data?.size ?: 0 },
            )

        LaunchedEffect(key1 = pagerState, block = {
            snapshotFlow { pagerState.currentPage }.distinctUntilChanged()
                .collect {
                    viewModel.setSongPosition(it, isTopTracks)
                }
        })

        LaunchedEffect(key1 = songPointer, block = {
            pagerState.animateScrollToPage(songPointer)
            if (!fromBottom) {
                songs?.get(songPointer)?.let {
                    viewModel.setIsPlaying(true)
                    viewModel.setCurrentSong(song = it)
                    val mediaItem =
                        MediaItem.fromUri(it.url)
                    with((requireActivity().application as MyApp).exoPlayer) {
                        setMediaItem(mediaItem)
                        prepare()
                        play()
                    }
                }
            } else {
                fromBottom = false
            }
        })

        HorizontalPager(
            state = pagerState,
            key = {
                songs?.get(it)?.id ?: "121"
            },
            modifier = modifier.fillMaxWidth(.75f),
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
                        .data("https://cms.samespace.com/assets/${songs?.get(index)?.cover}").build(),
                placeholder = painterResource(id = R.drawable.bg_gradient),
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
        TrackSliderView()
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
                        .clip(CircleShape)
                        .clickable {
                            viewModel.previousSong(isTopTracks)
                        }
                        .size(50.dp),
            )
            Icon(
                imageVector =
                    if (isSongPlaying) {
                        ImageVector.vectorResource(
                            R.drawable.ic_pause,
                        )
                    } else {
                        Icons.Default.PlayArrow
                    },
                contentDescription = "play-pause-song",
                tint = Color.Black,
                modifier =
                    Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .clickable {
                            if (isSongPlaying) {
                                (requireActivity().application as MyApp).exoPlayer.pause()
                            } else {
                                (requireActivity().application as MyApp).exoPlayer.play()
                            }
                            viewModel.setIsPlaying(!isSongPlaying)
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
                        .clip(CircleShape)
                        .clickable {
                            viewModel.nextSong(isTopTracks)
                        }
                        .size(50.dp),
            )
        }
    }

    @Composable
    fun TrackSliderView(trackColor: Color = Color.Black) {
        val currentPosition =
            remember {
                mutableLongStateOf(0L)
            }
        val sliderPosition =
            remember {
                mutableLongStateOf(0L)
            }
        val totalDuration =
            remember {
                mutableLongStateOf(0L)
            }

        LaunchedEffect(
            key1 = (requireActivity().application as MyApp).exoPlayer.currentPosition,
            key2 = (requireActivity().application as MyApp).exoPlayer.isPlaying,
        ) {
            delay(1000)
            currentPosition.longValue =
                (requireActivity().application as MyApp).exoPlayer.currentPosition
        }

        LaunchedEffect(currentPosition.longValue) {
            sliderPosition.longValue = currentPosition.longValue
        }

        LaunchedEffect((requireActivity().application as MyApp).exoPlayer.duration) {
            if ((requireActivity().application as MyApp).exoPlayer.duration > 0) {
                totalDuration.longValue =
                    (requireActivity().application as MyApp).exoPlayer.duration
            }
        }
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
        ) {
            TrackSlider(
                value = sliderPosition.longValue.toFloat(),
                onValueChange = {
                    sliderPosition.longValue = it.toLong()
                },
                onValueChangeFinished = {
                    currentPosition.longValue = sliderPosition.longValue
                    (requireActivity().application as MyApp).exoPlayer.seekTo(sliderPosition.longValue)
                },
                songDuration = totalDuration.longValue.toFloat(),
                trackColor = trackColor,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = (currentPosition.longValue).convertToText(),
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(8.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                val remainTime = totalDuration.longValue - currentPosition.longValue
                Text(
                    text = if (remainTime >= 0) remainTime.convertToText() else "",
                    modifier =
                        Modifier
                            .padding(8.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }

    private fun Long.convertToText(): String {
        val sec = this / 1000
        val minutes = sec / 60
        val seconds = sec % 60
        val minutesString =
            if (minutes < 10) {
                "0$minutes"
            } else {
                minutes.toString()
            }
        val secondsString =
            if (seconds < 10) {
                "0$seconds"
            } else {
                seconds.toString()
            }
        return "$minutesString:$secondsString"
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TrackSlider(
        value: Float,
        onValueChange: (newValue: Float) -> Unit,
        onValueChangeFinished: () -> Unit,
        songDuration: Float,
        trackColor: Color,
    ) {
        Slider(
            value = value,
            onValueChange = {
                onValueChange(it)
            },
            thumb = {},
            onValueChangeFinished = {
                onValueChangeFinished()
            },
            valueRange = 0f..songDuration,
            colors =
                SliderDefaults.colors(
                    thumbColor = trackColor,
                    activeTrackColor = trackColor,
                    inactiveTrackColor = colorResource(id = R.color.white50),
                ),
        )
    }
}
