package com.example.samespace.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import coil.compose.AsyncImage
import com.example.samespace.MyApp
import com.example.samespace.R
import com.example.samespace.databinding.FragmentHomeBinding
import com.google.android.material.tabs.TabLayoutMediator

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        setupViewPager()
        setupPlayerView()
        return binding.root
    }

    private fun setupViewPager() {
        val fragments =
            listOf(
                SongsListFragment(isTopTrack = false),
                SongsListFragment(isTopTrack = true),
            )
        val adapter = MainViewPagerAdapter(fragments, childFragmentManager, lifecycle)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "For You"
                1 -> tab.text = "Top Tracks"
            }
        }.attach()
    }

    private fun setupPlayerView() {
        viewModel.currentlyPlaying.observe(viewLifecycleOwner) {
            binding.showPlayerView = (it != null)
        }
        binding.playerView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { BottomPlayerView() }
        }
    }

    @Composable
    fun BottomPlayerView() {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
            color = MaterialTheme.colorScheme.background,
        ) {
            val song by viewModel.currentlyPlaying.observeAsState(null)
            val isSongPlaying by viewModel.isPlaying.observeAsState(true)
            val colors =
                Color(
                    android.graphics.Color.parseColor(
                        song?.data?.accent ?: "#000000",
                    ),
                )
            val gradient =
                Brush.horizontalGradient(
                    colors =
                        listOf(
                            colors.copy(alpha = .7f),
                            colors.copy(alpha = .90f),
                            colors,
                        ),
                )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .background(gradient)
                        .clickable {
                            val fragment =
                                SongPlayerFragment(isTopTracks = false, true)
                            fragment.show(
                                requireActivity().supportFragmentManager,
                                "SongPlayerFragment",
                            )
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "rotate-anim")
                    val angle by infiniteTransition.animateFloat(
                        initialValue = 0F,
                        targetValue = 360F,
                        animationSpec =
                            infiniteRepeatable(
                                animation = tween(2000, easing = LinearEasing),
                            ),
                        label = "rotate-angle",
                    )
                    AsyncImage(
                        model = "https://cms.samespace.com/assets/${song?.data?.cover}",
                        contentDescription = "cover-image-current",
                        placeholder = painterResource(id = R.drawable.bg_gradient),
                        modifier =
                            Modifier
                                .then(
                                    if (isSongPlaying) {
                                        Modifier
                                            .graphicsLayer {
                                                rotationZ = angle
                                            }
                                    } else {
                                        Modifier
                                    },
                                )
                                .size(45.dp)
                                .clip(
                                    CircleShape,
                                ),
                        contentScale = ContentScale.Crop,
                    )
                    Text(
                        text = song?.data?.name ?: "",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W500,
                    )
                }
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
                            .size(38.dp)
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
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
