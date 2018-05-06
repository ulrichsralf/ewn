# ewn
einstein würfelt nicht spiel

https://de.wikipedia.org/wiki/EinStein_w%C3%BCrfelt_nicht

* Spielfeld ist in Koordinaten eingeteilt, x und y von 1-5
* Die Steine sind nummeriert von 1-6
* Beim ersten Zug wird die Startposition übertragen
* Die Startposition kann frei gewählt werden, sie wird als String codiert übertragen: 311 512 113 221 422 631
* Ein Zug wird genauso codiert: 534 (Stein 5 auf x3 y4)
* Der Server gibt den Würfel vor
* Jeder Spieler ist muss selbst überprüfen, ob der Gegner illegale Züge macht


Befehle:
- hilfe
- login <name>
- logout
- liste
- werBinIch (lol)
- spiel <gegner>

Server Response Codes ( reverse engineered ):
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
    
Server :  vpf.mind-score.de 1078
