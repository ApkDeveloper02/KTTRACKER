package com.example.kttrackingapp

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCancellationBehavior
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.kttrackingapp.ui.theme.blueApp
import com.example.kttrackingapp.ui.theme.inActiveTextColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun checkForInternet(context: Context) : Boolean
{

    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


        val network = connectivityManager.activeNetwork ?: return false

        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

            else -> false
        }
    } else {
        @Suppress("DEPRECATION") val networkInfo =
            connectivityManager.activeNetworkInfo ?: return false
        @Suppress("DEPRECATION")
        return networkInfo.isConnected
    }
}

val Int.scaledSp  @Composable get() = (this / LocalDensity.current.fontScale).sp

@Composable
fun CommonText(
    text : String,
    fontSize : Int = 14,
    color: Color = Color.Black,
    textAlign : TextAlign = TextAlign.Center,
    fontWeight: FontWeight? = FontWeight.Medium,
    lineHeight: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    textDecoration: TextDecoration? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    fontFamily: FontFamily = FontFamily(Font(R.font.google_sans)),
    modifier : Modifier = Modifier,
)
{
    Text(
        text = text ,
        fontSize = fontSize.scaledSp ,
        color = color ,
        textAlign = textAlign ,
        fontWeight = fontWeight,
        lineHeight = lineHeight,
        textDecoration = textDecoration,
        fontFamily = fontFamily,
        maxLines = maxLines,
        overflow = overflow,
        modifier = modifier
    )
}


@Composable
fun LottieAnimation_Json(animation :Int ,
                         contentScale: ContentScale ,
                         modifier: Modifier , speed : Float = 1f ,
                         reverse : Boolean = true)
{

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animation))

    if (composition == null) {
        // Show loader while animation is being prepared
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        // Animation is ready
        val progress by animateLottieCompositionAsState(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            isPlaying = true,
            reverseOnRepeat = reverse,
            speed = speed,
            cancellationBehavior = LottieCancellationBehavior.OnIterationFinish
        )

        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = modifier,
            contentScale = contentScale
        )
    }
}


fun openInGoogleMaps(context: Context, lat: Double, lng: Double) {
    val gmmIntentUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")
    context.startActivity(mapIntent)
}


fun shareLocation(context: Context, lat: Double, lng: Double, dateTime: Pair<String, String>, ) {
    val uri = "https://maps.google.com/?q=$lat,$lng"

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "My location on ${dateTime.first} (${dateTime.second}) is : $uri")
    }

    context.startActivity(Intent.createChooser(intent, "Share Location"))
}


fun formatTimestamp(timestampString: String): Pair<String, String> {
    val timestamp = timestampString.toLong()

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    val date = Date(timestamp)

    return Pair(
        dateFormat.format(date),
        timeFormat.format(date)
    )
}


@Composable
fun CommonLoader()
{
    Box(modifier = Modifier
        .fillMaxSize()
        .noRippleClickable {} ,
        contentAlignment = Alignment.Center)
    {
        Column(verticalArrangement = Arrangement.Center ,
            horizontalAlignment = Alignment.CenterHorizontally)
        {
            LottieAnimation_Json(
                animation = R.raw.loader ,
                reverse = false ,
                contentScale = ContentScale.Fit,
                speed = 0.65f,
                modifier = Modifier.size(75.dp)
            )

            Spacer(modifier = Modifier.height(25.dp))

            CommonText(
                text = "Loading...",
                fontSize = 16 ,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }
    }
}


@Composable
fun NoInternet_UI(onRetry: () -> Unit)
{
    Column(modifier = Modifier.fillMaxSize() ,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally)
    {
        Image(painter = painterResource(R.drawable.nointernet) , "" ,
            modifier = Modifier.size(120.dp))

        Spacer(modifier = Modifier.height(30.dp))

        CommonText(
            "No Internet Connection",
            fontSize = 18 ,
            color = Color.Black ,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(15.dp))

        CommonText(
            "Need Internet to explore map" ,
            fontSize = 14 ,
            color = inActiveTextColor,
            textAlign = TextAlign.Center ,
            modifier = Modifier
        )

        Spacer(modifier = Modifier.height(50.dp))

        Box(modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .fillMaxWidth(0.75f)
            .wrapContentHeight()
            .background(blueApp)
            .noRippleClickable {
                onRetry()
            }
            .padding(vertical = 15.dp),
            contentAlignment = Alignment.Center)
        {
            CommonText(
                "Try Again" ,
                fontSize = 16 ,
                color = Color.White
            )
        }

    }
}