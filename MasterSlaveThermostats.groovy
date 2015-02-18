/**
 *  Turn off thermostat when mode changed to X
 *
 *  Copyright 2014 skp19
 *
 */
definition(
        name: "Master-Slave thermostats",
        namespace: "belmass@gmail.com",
        author: "Andrei Zharov",
        description: "Have one thermostat control another one",
        category: "Convenience",
        iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
        iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("Master thermostat") {
        input "master", "capability.thermostat", multiple: false
    }
    section("Controls this thermostat") {
        input "slave", "capability.thermostat", multiple: false
        input "notify", "bool", title: "Notify?"
    }
}

def state = [];

/**
 * Triggered by SmartThings API when app is installed
 * @return
 */
def installed() {
    subscribeToEvents();
}

/**
 * Triggered by SmartThings API when app is updated
 * @return
 */
def updated() {
    // unsubscribe from active events
    unsubscribe();
    // subscribe to all events
    subscribeToEvents();
}

/**
 * Monitor changes in thermostat values
 * @return
 */
def subscribeToEvents() {
    subscribe(master, "heatingSetpoint", heatingSetpointHandler)
    subscribe(master, "coolingSetpoint", coolingSetpointHandler)
}

/**
 * Trigger for `heatingSetpoint` event from master
 * @param evt SmartThings event
 * @return
 */
def heatingSetpointHandler(evt) {
    log.debug "heatingSetpoint: $evt"
    log.debug "heatingSetpoint value: $evt.value"
    state.heatingTemperature = evt.value.toDouble();
    log.debug "Setting slave to heat mode";
    slave.heat();
    log.debug "Scheduling setHeatingSetpoint in 20s";
    runIn(20, "setHeatingSetpoint");
}
def setHeatingSetpoint() {
    log.debug "Settung scheduled heating temperature value: $state.heatingTemperature"
    slave.setHeatingSetpoint(state.heatingTemperature);
    sendMessage("Slave thermostat has been changed to $state.heatingTemperature F in heat mode");
}

/**
 * Trigger for `coolingSetpoint` event from master
 * @param evt SmartThings event
 * @return
 */
def coolingSetpointHandler(evt) {
    log.debug "coolingSetpoint: $evt"
    log.debug "coolingSetpoint value: $evt.value";
    state.coolingTemperature = evt.value.toDouble();
    log.debug "Setting slave to cool mode";
    slave.cool();
    log.debug "Scheduling setCoolingSetpoint in 20s";
    runIn(20, "setCoolingSetpoint");
}

def setCoolingSetpoint() {
    log.debug "Settung scheduled cooling temperature value: $state.heatingTemperature"
    slave.setHeatingSetpoint(state.coolingTemperature);
    sendMessage("Slave thermostat has been changed to $state.coolingTemperature F in cool mode");
}

/**
 * Send push notification if requested
 * @param msg Message to send
 * @return
 */
def sendMessage(msg) {
    if (notify) {
        sendPush msg
    }
}
