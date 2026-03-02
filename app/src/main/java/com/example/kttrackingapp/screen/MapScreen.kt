package com.example.kttrackingapp.screen

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kttrackingapp.CommonLoader
import com.example.kttrackingapp.NoInternet_UI
import com.example.kttrackingapp.R
import com.example.kttrackingapp.checkForInternet
import com.example.kttrackingapp.noRippleClickable
import com.example.kttrackingapp.roomDB.roomVM.UserDetailVM
import com.example.kttrackingapp.ui.theme.blueApp
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


@Composable
fun MapScreen(context: Context , dbVM: UserDetailVM)
{

    var network_status = remember{ mutableStateOf(checkForInternet(context)) }


    var loader by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1500)
        loader = true
    }


    if(!loader)
    {
        CommonLoader()
    }
    else
    {
        if(!network_status.value)
        {
            NoInternet_UI {
                network_status.value = checkForInternet(context)
            }
        }
        else
            OpenStreetMapWithMyLocation(dbVM)
    }

}

@SuppressLint("MissingPermission")
@Composable
fun OpenStreetMapWithMyLocation(dbVM: UserDetailVM) {

    DisposableEffect(Unit) {
        onDispose {
            dbVM.selectedMapLocation.value = null
        }
    }


    val context = LocalContext.current

    val selectedLocation by remember {
        dbVM.selectedMapLocation
    }

    var mapView by remember { mutableStateOf<MapView?>(null) }
    var locationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }

    val fusedClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var staticMarkers by remember { mutableStateOf<List<Marker>>(emptyList()) }

    var isPlaying by remember { mutableStateOf(false) }
    var playbackIndex by remember { mutableStateOf(0) }

    var movingMarker by remember { mutableStateOf<Marker?>(null) }
    var playbackPolyline by remember { mutableStateOf<Polyline?>(null) }

    val userDetail by dbVM.userData.collectAsStateWithLifecycle()

    val points = remember(userDetail) {
        userDetail.map {
            GeoPoint(
                it.user_latitude.toDouble(),
                it.user_longitude.toDouble()
            )
        }
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osm_pref", 0)
        )
    }

   /* LaunchedEffect(points, mapView) {

        val map = mapView ?: return@LaunchedEffect

        // remove old markers
        staticMarkers.forEach {
            map.overlays.remove(it)
        }

        val newMarkers = points.mapIndexed { index, point ->

            Marker(map).apply {

                position = point

                icon = ContextCompat.getDrawable(
                    context,
                    R.drawable.locationn
                )

                setAnchor(
                    Marker.ANCHOR_CENTER,
                    Marker.ANCHOR_BOTTOM
                )

                // rotate toward next point
                if (index < points.lastIndex) {

                    rotation =
                        getBearing(
                            points[index],
                            points[index + 1]
                        ).toFloat()
                }

                setOnMarkerClickListener { marker, _ ->

                    map.controller.animateTo(marker.position)

                    true
                }

                map.overlays.add(this)
            }
        }

        staticMarkers = newMarkers

        map.invalidate()
    }*/

    LaunchedEffect(points,mapView, selectedLocation) {

        val map = mapView ?: return@LaunchedEffect
        val selected = selectedLocation ?: return@LaunchedEffect

        val point = GeoPoint(
            selected.latitude,
            selected.longitude
        )

        val marker = Marker(map).apply {

            position = point

            title = selected.date
            snippet = selected.time

            setAnchor(
                Marker.ANCHOR_CENTER,
                Marker.ANCHOR_BOTTOM
            )

            icon = ContextCompat.getDrawable(
                context,
                R.drawable.locationn
            )

            setOnMarkerClickListener { m, _ ->
                m.showInfoWindow()
                true
            }
        }

        map.overlays.add(marker)

        map.controller.animateTo(point)
        map.controller.setZoom(18.0)

        marker.showInfoWindow()

        map.invalidate()
    }


    LaunchedEffect(isPlaying) {

        if (isPlaying && points.isNotEmpty()) {

            movingMarker?.position = points.first()
            movingMarker?.setVisible(true)

            playbackPolyline?.setPoints(emptyList())
            playbackIndex = 0

            val routePoints = mutableListOf<GeoPoint>()

            while (playbackIndex < points.size && isPlaying) {

                val point = points[playbackIndex]

                movingMarker?.position = point

                if (playbackIndex < points.lastIndex) {
                    movingMarker?.rotation =
                        getBearing(points[playbackIndex], points[playbackIndex + 1]).toFloat()
                }

                routePoints.add(point)
                playbackPolyline?.setPoints(routePoints)

                mapView?.controller?.animateTo(point)
                mapView?.invalidate()

                playbackIndex++
                delay(1000)
            }

            movingMarker?.setVisible(false)
            mapView?.invalidate()

            isPlaying = false
        }
    }

    Box(Modifier.fillMaxSize()) {

        AndroidView(
            modifier = Modifier.fillMaxSize(),

            factory = {

                MapView(context).apply {

                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)

                    controller.setZoom(15.0)

                    // blue dot
                    val overlay = MyLocationNewOverlay(
                        GpsMyLocationProvider(context),
                        this
                    )
                    overlay.enableMyLocation()
                    overlays.add(overlay)

                    locationOverlay = overlay

                    // moving playback marker
                    movingMarker = Marker(this).apply {

                        icon = ContextCompat.getDrawable(
                            context,
                            R.drawable.locationn
                        )

                        setAnchor(
                            Marker.ANCHOR_CENTER,
                            Marker.ANCHOR_CENTER
                        )

                        setVisible(false)
                    }

                    overlays.add(movingMarker)

                    // playback polyline
                    playbackPolyline = Polyline().apply {

                        outlinePaint.color = Color.BLUE
                        outlinePaint.strokeWidth = 10f
                    }

                    overlays.add(playbackPolyline)

                    mapView = this
                }
            },

            update = { map ->

                if (!isPlaying) {

                    if (points.isNotEmpty()) {

                        map.controller.setCenter(points.last())
                        map.controller.setZoom(17.0)
                    }
                    else {

                        val defaultPoint =
                            GeoPoint(11.0168, 76.9558)

                        map.controller.setCenter(defaultPoint)
                        map.controller.setZoom(10.0)
                    }
                }
            }
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(15.dp)
        )
        {
            Box(modifier = Modifier.clip(CircleShape)
                .wrapContentSize()
                .background(androidx.compose.ui.graphics.Color.White)
                .noRippleClickable {
                    fusedClient.lastLocation
                        .addOnSuccessListener { location ->

                            location?.let {

                                val userPoint =
                                    GeoPoint(it.latitude, it.longitude)

                                mapView?.controller?.animateTo(userPoint)
                                mapView?.controller?.setZoom(18.0)

                                locationOverlay?.enableFollowLocation()
                            }
                        }
                },
                contentAlignment = Alignment.Center)
            {
                Image(painter = painterResource(R.drawable.currentloc) , "" ,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(25.dp))
            }



            if(!points.isNullOrEmpty())
            {
                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.clip(CircleShape)
                    .wrapContentSize()
                    .background(androidx.compose.ui.graphics.Color.White)
                    .noRippleClickable {

                        if (isPlaying) {
                            isPlaying = false
                            movingMarker?.setVisible(false)
                            mapView?.invalidate()
                        } else {
                            isPlaying = true
                        }

                    },
                    contentAlignment = Alignment.Center)
                {
                    Image(painter = painterResource(if(isPlaying) R.drawable.pause else R.drawable.play) , "" ,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(25.dp))
                }
            }
        }
    }
}


fun getBearing(start: GeoPoint, end: GeoPoint): Double {

    val lat1 = Math.toRadians(start.latitude)
    val lon1 = Math.toRadians(start.longitude)

    val lat2 = Math.toRadians(end.latitude)
    val lon2 = Math.toRadians(end.longitude)

    val dLon = lon2 - lon1

    val y = sin(dLon) * cos(lat2)
    val x = cos(lat1) * sin(lat2) -
            sin(lat1) * cos(lat2) * cos(dLon)

    val bearing = Math.toDegrees(atan2(y, x))

    return (bearing + 360) % 360
}