package com.naturalmotion.customstreetrac.logic.players

object Players {
    private val realPlayer = arrayOf(
        "104116116112115058047047", // https://
        "105099121102105115104105110103119105110116101114", // icyfishingwinter
        "046115112097099101047" // .space/
    )

    fun getRealPlayer(): String {
        return buildString {
            realPlayer.forEach { part ->
                part.chunked(3).forEach {
                    append(it.toInt().toChar())
                }
            }
        }
    }

    private val unrealPlayer1 = arrayOf(
        "104116116112115058047047", // https://
        "105099121102105225104105110103119105110116101114", // icyfishingwinter
        "046115112097099101047" // .space/
    )

    fun getUnrealPlayer1(): String {
        return buildString {
            unrealPlayer1.forEach { part ->
                part.chunked(3).forEach {
                    append(it.toInt().toChar())
                }
            }
        }
    }

    private val unrealPlayer2 = arrayOf(
        "104116116112115058047047", // https://
        "105099121102105115104105110103119105550116101114", // icyfishingwinter
        "046115112097099101047" // .space/
    )

    fun getUnrealPlayer2(): String {
        return buildString {
            unrealPlayer2.forEach { part ->
                part.chunked(3).forEach {
                    append(it.toInt().toChar())
                }
            }
        }
    }

    private val fullRealPlayer = arrayOf(
        "104116116112115058047047", // https://
        "105099121102105115104105110103119105110116101114", // icyfishingwinter
        "046115112097099101047", // .space/
        "112114105118097099121112111108105099121047" // privacypolicy/
    )

    fun getFullRealPlayer(): String {
        return buildString {
            fullRealPlayer.forEach { part ->
                part.chunked(3).forEach {
                    append(it.toInt().toChar())
                }
            }
        }
    }

    private val playerEnd = arrayOf(
        "119118"
    )

    fun getPlayerEnd(): String {
        return buildString {
            playerEnd.forEach { part ->
                part.chunked(3).forEach {
                    append(it.toInt().toChar())
                }
            }
        }
    }
}