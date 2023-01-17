package dev.veeso.opentapowearos.tapo.api.request.params

enum class Color {
    COOL_WHITE,
    DAYLIGHT,
    IVORY,
    WARM_WHITE,
    INCANDESCENT,
    CANDLELIGHT,
    SNOW,
    GHOST_WHITE,
    ALICE_BLUE,
    LIGHT_GOLDENROD,
    LEMON_CHIFFON,
    ANTIQUE_WHITE,
    GOLD,
    PERU,
    CHOCOLATE,
    SANDY_BROWN,
    CORAL,
    PUMPKIN,
    TOMATO,
    VERMILION,
    ORANGE_RED,
    PINK,
    CRIMSON,
    DARK_RED,
    HOT_PINK,
    SMITTEN,
    MEDIUM_PURPLE,
    BLUE_VIOLET,
    INDIGO,
    LIGHT_SKY_BLUE,
    CORNFLOWER_BLUE,
    ULTRAMARINE,
    DEEP_SKY_BLUE,
    AZURE,
    NAVY_BLUE,
    LIGHT_TURQUOISE,
    AQUAMARINE,
    TURQUOISE,
    LIGHT_GREEN,
    LIME,
    FOREST_GREEN;

    fun getConfig(): ColorConfig {
        return when (this) {
            COOL_WHITE -> ColorConfig(null, null, 4000u)
            DAYLIGHT -> ColorConfig(null, null, 5000u)
            IVORY -> ColorConfig(null, null, 6000u)
            WARM_WHITE -> ColorConfig(null, null, 3000u)
            INCANDESCENT -> ColorConfig(null, null, 2700u)
            CANDLELIGHT -> ColorConfig(null, null, 2500u)
            SNOW -> ColorConfig(null, null, 6500u)
            GHOST_WHITE -> ColorConfig(null, null, 6500u)
            ALICE_BLUE -> ColorConfig(208u, 5u, null)
            LIGHT_GOLDENROD -> ColorConfig(54u, 28u, null)
            LEMON_CHIFFON -> ColorConfig(54u, 19u, null)
            ANTIQUE_WHITE -> ColorConfig(null, null, 5500u)
            GOLD -> ColorConfig(50u, 100u, null)
            PERU -> ColorConfig(29u, 69u, null)
            CHOCOLATE -> ColorConfig(30u, 100u, null)
            SANDY_BROWN -> ColorConfig(27u, 60u, null)
            CORAL -> ColorConfig(16u, 68u, null)
            PUMPKIN -> ColorConfig(24u, 90u, null)
            TOMATO -> ColorConfig(9u, 72u, null)
            VERMILION -> ColorConfig(4u, 77u, null)
            ORANGE_RED -> ColorConfig(16u, 100u, null)
            PINK -> ColorConfig(349u, 24u, null)
            CRIMSON -> ColorConfig(348u, 90u, null)
            DARK_RED -> ColorConfig(0u, 100u, null)
            HOT_PINK -> ColorConfig(330u, 58u, null)
            SMITTEN -> ColorConfig(329u, 67u, null)
            MEDIUM_PURPLE -> ColorConfig(259u, 48u, null)
            BLUE_VIOLET -> ColorConfig(271u, 80u, null)
            INDIGO -> ColorConfig(274u, 100u, null)
            LIGHT_SKY_BLUE -> ColorConfig(202u, 46u, null)
            CORNFLOWER_BLUE -> ColorConfig(218u, 57u, null)
            ULTRAMARINE -> ColorConfig(254u, 100u, null)
            DEEP_SKY_BLUE -> ColorConfig(195u, 100u, null)
            AZURE -> ColorConfig(210u, 100u, null)
            NAVY_BLUE -> ColorConfig(240u, 100u, null)
            LIGHT_TURQUOISE -> ColorConfig(180u, 26u, null)
            AQUAMARINE -> ColorConfig(159u, 50u, null)
            TURQUOISE -> ColorConfig(174u, 71u, null)
            LIGHT_GREEN -> ColorConfig(120u, 39u, null)
            LIME -> ColorConfig(75u, 100u, null)
            FOREST_GREEN -> ColorConfig(120u, 75u, null)
        }
    }

}

class ColorConfig(hue: UInt?, saturation: UInt?, colorTemp: UInt?) {

    val hue: UInt?
    val saturation: UInt?
    val colorTemp: UInt?

    init {
        this.hue = hue
        this.saturation = saturation
        this.colorTemp = colorTemp
    }

}
