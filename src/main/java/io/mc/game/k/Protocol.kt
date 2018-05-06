package io.mc.game.k


sealed class C(val key: String) {
    override fun toString(): String {
        return key
    }
}

class ConsoleCommand(val cc: String) : C(cc)
class Login(val name: String) : C("login $name")
object Logout : C("logout")
object PlayerList : C("liste")
class RequestGame(val opp: String) : C("spiel $opp")
object WhoAmI : C("werbinich")
class MoveC(m: List<Move>) : C(m.map { it.toString() }.joinToString(separator = " "))
object Yes : C("ja")
object No : C("nein")
object Help : C("hilfe")


enum class R(val code: String) {
    ParseError(""),
    Success("B"),
    Message("M"),
    Move("Z"),
    GameRequest("Q"),
    UnknownCommand("E001"),
    LoginRequest("E101"),
    NameConflict("E102"),
    LoginNameMissing("E103"),
    AlreadyLoggedIn("E104"),
    GameRequestRejected("E201"),
    RequestNameMissing("E202"),
    PlayerNotFound("E203"),
    PlayerNotAvailable("E204"),
    SelfRequestForbidden("E205"),
    GameRequestPending("206"),
    AlreadyRequested("E207"),
    Cancelled("E208"),
    MoveTimout("E301"),
    IdleTimeout("E302"),
    GameRequestTimeout("E303")
}

class WrongMove : Exception("Wrong move")
data class Message(val sender: String, val code: String, val message: String)
typealias MoveMsg = Triple<Board.Directon, List<Move>, Int>

fun Message.isConnected() = message.contains(connectMarker)
fun Message.isLoggedIn() = message.contains(loggedInMarker)
fun Message.isListe() = message.contains(listeMarker)
fun Message.isEmptyListe() = message.contains(listeMarker) &&
        message.substringAfter(listeMarker) in arrayOf(emptyListeMarker, nobodyFree)

fun Message.getPlayerListe() = if (isEmptyListe()) emptyList()
else message.substringAfter(listeMarker).split(" ").filterNot { it.isEmpty() }

fun Message.isStart() = message.contains(gameStart)
fun Message.parseMoves() = if (message == "E01") throw WrongMove() else
    message.split(" ").filterNot { it.isEmpty() }.map { Move(it) }
fun Message.isDice() = message.startsWith(diceMarker)
fun Message.parseDice() = message.substringAfter(diceMarker).toInt()

val connectMarker = "Verbindung zum Server erstellt"
val loggedInMarker = "Sie sind angemeldet"
val listeMarker = "Folgende Spieler waeren bereit zu spielen: "
val emptyListeMarker = "Sonst keiner da!"
val nobodyFree = "Niemand frei!"
val requestMarker = "Hole Einverstaendnis von"
val gameStart = "Spiel startet."
val diceMarker = "Würfel: "

fun String.parseMessage(): Message {
    val z = contains("Z>") // TODO
    val w = contains(diceMarker) && !contains("(")
    return split(" ").let {
        if (z)
            if (w)
                Message(it[0], it[1].dropLast(1), drop(indexOf(">") + 2))
            else
                Message(it[0], it[3].dropLast(1), drop(indexOf(">") + 2))
        else
            Message(it[0], it[1].dropLast(1), drop(indexOf(">") + 2))
    }
}

fun String.parseResponseCode(): R {
    return parseMessage().let { c ->
        R.values().firstOrNull { it.code == c.code } ?: R.ParseError
    }

}

