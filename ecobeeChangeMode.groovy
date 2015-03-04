/**
 *  ecobeeChangeMode
 *  Andrei Zharov
 *
 *  Change Ecobee Mode based on ST Mode change
 */
definition(
        name: "ecobeeChangeMode",
        namespace: "belmass@gmail.com",
        author: "Andrei Zharov",
        description: "Change the mode automatically at the ecobee thermostat(s)",
        category: "My Apps",
        iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/ecobee.png",
        iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/ecobee@2x.png"
)

preferences {
    section("Turn off these thermostats") {
        input "thermostats", "capability.thermostat", multiple: true
        input "notify", "bool", title: "Notify?"
    }
}


def installed() {
    subscribe(location, changeMode)
}

def updated() {
    unsubscribe()
    subscribe(location, changeMode)
}


def changeMode(evt) {
    def message
    def newMode = evt.value.trim().toUpperCase()
    def givenMode

    log.debug "New mode is $newMode"
    if (newMode == "AWAY") {
        givenMode = "away"
    } else if (newMode == "HOME") {
        givenMode = "home"
    } else if (newMode == "NIGHT") {
        givenMode = "sleep"
    } else if (newMode == "MORNING") {
        givenMode = "awake"
    }
    message = "Setting thermostat(s) to $givenMode"
    log.debug message
    thermostats?."$givenMode"()
    sendMessage(message)
}

def sendMessage(msg) {
    if (notify) {
        sendPush msg
    }
}
