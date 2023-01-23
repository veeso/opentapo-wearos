package dev.veeso.opentapowearos.view

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
            COOL_WHITE -> ColorConfig(null, null, 4000)
            DAYLIGHT -> ColorConfig(null, null, 5000)
            IVORY -> ColorConfig(null, null, 6000)
            WARM_WHITE -> ColorConfig(null, null, 3000)
            INCANDESCENT -> ColorConfig(null, null, 2700)
            CANDLELIGHT -> ColorConfig(null, null, 2500)
            SNOW -> ColorConfig(null, null, 6500)
            GHOST_WHITE -> ColorConfig(null, null, 6500)
            ALICE_BLUE -> ColorConfig(208, 5, null)
            LIGHT_GOLDENROD -> ColorConfig(54, 28, null)
            LEMON_CHIFFON -> ColorConfig(54, 19, null)
            ANTIQUE_WHITE -> ColorConfig(null, null, 5500)
            GOLD -> ColorConfig(50, 100, null)
            PERU -> ColorConfig(29, 69, null)
            CHOCOLATE -> ColorConfig(30, 100, null)
            SANDY_BROWN -> ColorConfig(27, 60, null)
            CORAL -> ColorConfig(16, 68, null)
            PUMPKIN -> ColorConfig(24, 90, null)
            TOMATO -> ColorConfig(9, 72, null)
            VERMILION -> ColorConfig(4, 77, null)
            ORANGE_RED -> ColorConfig(16, 100, null)
            PINK -> ColorConfig(349, 24, null)
            CRIMSON -> ColorConfig(348, 90, null)
            DARK_RED -> ColorConfig(0, 100, null)
            HOT_PINK -> ColorConfig(330, 58, null)
            SMITTEN -> ColorConfig(329, 67, null)
            MEDIUM_PURPLE -> ColorConfig(259, 48, null)
            BLUE_VIOLET -> ColorConfig(271, 80, null)
            INDIGO -> ColorConfig(274, 100, null)
            LIGHT_SKY_BLUE -> ColorConfig(202, 46, null)
            CORNFLOWER_BLUE -> ColorConfig(218, 57, null)
            ULTRAMARINE -> ColorConfig(254, 100, null)
            DEEP_SKY_BLUE -> ColorConfig(195, 100, null)
            AZURE -> ColorConfig(210, 100, null)
            NAVY_BLUE -> ColorConfig(240, 100, null)
            LIGHT_TURQUOISE -> ColorConfig(180, 26, null)
            AQUAMARINE -> ColorConfig(159, 50, null)
            TURQUOISE -> ColorConfig(174, 71, null)
            LIGHT_GREEN -> ColorConfig(120, 39, null)
            LIME -> ColorConfig(75, 100, null)
            FOREST_GREEN -> ColorConfig(120, 75, null)
        }
    }

    companion object {

        fun fromString(s: String): Color {
            return when (s) {
                "daylight" -> COOL_WHITE
                "cool white" -> DAYLIGHT
                "ivory" -> IVORY
                "warm white" -> WARM_WHITE
                "incandescent" -> INCANDESCENT
                "candlelight" -> CANDLELIGHT
                "snow" -> SNOW
                "ghost white" -> GHOST_WHITE
                "alice blue" -> ALICE_BLUE
                "light goldenrod" -> LIGHT_GOLDENROD
                "lemon chiffon" -> LEMON_CHIFFON
                "antique white" -> ANTIQUE_WHITE
                "gold" -> GOLD
                "peru" -> PERU
                "chocolate" -> CHOCOLATE
                "sandy brown" -> SANDY_BROWN
                "coral" -> CORAL
                "pumpkin" -> PUMPKIN
                "tomato" -> TOMATO
                "vermilion" -> VERMILION
                "orange red" -> ORANGE_RED
                "pink" -> PINK
                "crimson" -> CRIMSON
                "dark red" -> DARK_RED
                "hot pink" -> HOT_PINK
                "smitten" -> SMITTEN
                "medium purple" -> MEDIUM_PURPLE
                "blue violet" -> BLUE_VIOLET
                "indigo" -> INDIGO
                "light sky blue" -> LIGHT_SKY_BLUE
                "cornflower blue" -> CORNFLOWER_BLUE
                "ultramarine" -> ULTRAMARINE
                "deep sky blue" -> DEEP_SKY_BLUE
                "azure" -> AZURE
                "navy blue" -> NAVY_BLUE
                "light turquoise" -> LIGHT_TURQUOISE
                "aquamarine" -> AQUAMARINE
                "turquoise" -> TURQUOISE
                "light green" -> LIGHT_GREEN
                "lime" -> LIME
                "forest green" -> FOREST_GREEN
                else -> throw Exception(String.format("Unknown color: %s", s))
            }
        }

        val COLOR_LIST = listOf(
            "cool white",
            "daylight",
            "ivory",
            "warm white",
            "incandescent",
            "candlelight",
            "snow",
            "ghost white",
            "alice blue",
            "light goldenrod",
            "lemon chiffon",
            "antique white",
            "gold",
            "peru",
            "chocolate",
            "sandy brown",
            "coral",
            "pumpkin",
            "tomato",
            "vermilion",
            "orange red",
            "pink",
            "crimson",
            "dark red",
            "hot pink",
            "smitten",
            "medium purple",
            "blue violet",
            "indigo",
            "light sky blue",
            "cornflower blue",
            "ultramarine",
            "deep sky blue",
            "azure",
            "navy blue",
            "light turquoise",
            "aquamarine",
            "turquoise",
            "light green",
            "lime",
            "forest green",
        )
    }

}

class ColorConfig(hue: Int?, saturation: Int?, colorTemp: Int?) {

    val hue: Int?
    val saturation: Int?
    val colorTemp: Int?

    init {
        this.hue = hue
        this.saturation = saturation
        this.colorTemp = colorTemp
    }

}
