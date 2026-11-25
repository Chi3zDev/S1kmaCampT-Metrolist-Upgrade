package com.metrolist.android.streak

/** Normalized silhouette geometry, shared by the live flame and exported card. */
internal data class FlameShape(
    val tipX: Float,
    val tipY: Float,
    val notchX: Float,
    val notchY: Float,
    val rightX: Float,
    val rightY: Float,
    val leftControlX: Float,
    val middleTipY: Float,
    val middleNotchY: Float,
    val coreTipY: Float,
)

internal val flameShapes =
    listOf(
        FlameShape(.54f, .10f, .62f, .39f, .73f, .37f, .300f, .310f, .500f, .570f),
        FlameShape(.61f, .08f, .60f, .38f, .79f, .40f, .309f, .319f, .508f, .578f),
        FlameShape(.55f, .07f, .63f, .47f, .84f, .32f, .318f, .328f, .516f, .586f),
        FlameShape(.44f, .05f, .58f, .43f, .79f, .28f, .327f, .337f, .524f, .594f),
        FlameShape(.53f, .06f, .58f, .40f, .84f, .31f, .336f, .346f, .532f, .602f),
        FlameShape(.45f, .04f, .61f, .46f, .82f, .25f, .345f, .355f, .540f, .610f),
        FlameShape(.56f, .025f, .57f, .38f, .86f, .22f, .354f, .364f, .548f, .618f),
    )

internal fun flameShape(variant: Int): FlameShape = flameShapes[variant.coerceIn(flameShapes.indices)]
