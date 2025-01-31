/**
 *  MyQ Lite
 *
 *  Copyright 2015 Jason Mok/Brian Beaird/Barry Burke
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Last Updated : 12/9/2016
 *
 */
definition(
	name: "MyQ Lite",
	namespace: "brbeaird",
	author: "Jason Mok/Brian Beaird/Barry Burke",
	description: "Integrate MyQ with Smartthings",
	category: "SmartThings Labs",
	iconUrl:   "http://smartthings.copyninja.net/icons/MyQ@1x.png",
	iconX2Url: "http://smartthings.copyninja.net/icons/MyQ@2x.png",
	iconX3Url: "http://smartthings.copyninja.net/icons/MyQ@3x.png"
)

preferences {
	page(name: "prefLogIn", title: "MyQ")    
	page(name: "prefListDevices", title: "MyQ")
    page(name: "prefSensor1", title: "MyQ")
    page(name: "prefSensor2", title: "MyQ")
    page(name: "prefSensor3", title: "MyQ")
    page(name: "prefSensor4", title: "MyQ")
}

/* Preferences */
def prefLogIn() {
	def showUninstall = username != null && password != null 
	return dynamicPage(name: "prefLogIn", title: "Connect to MyQ", nextPage:"prefListDevices", uninstall:showUninstall, install: false) {
		section("Login Credentials"){
			input("username", "email", title: "Username", description: "MyQ Username (email address)")
			input("password", "password", title: "Password", description: "MyQ password")
		}
		section("Gateway Brand"){
			input(name: "brand", title: "Gateway Brand", type: "enum",  metadata:[values:["Liftmaster","Chamberlain","Craftsman"]] )
		}
	}
}

def prefListDevices() {
	if (forceLogin()) {
		def doorList = getDoorList()		
		if ((doorList)) {
			return dynamicPage(name: "prefListDevices",  title: "Devices", nextPage:"prefSensor1", install:false, uninstall:true) {
				if (doorList) {
					section("Select which garage door/gate to use"){
						input(name: "doors", type: "enum", required:false, multiple:true, metadata:[values:doorList])
					}
				} 
			}
		} else {
			def devList = getDeviceList()
			return dynamicPage(name: "prefListDevices",  title: "Error!", install:false, uninstall:true) {
				section(""){
					paragraph "Could not find any supported device(s). Please report to author about these devices: " +  devList
				}
			}
		}  
	} else {
		return dynamicPage(name: "prefListDevices",  title: "Error!", install:false, uninstall:true) {
			section(""){
				paragraph "The username or password you entered is incorrect. Try again. " 
			}
		}  
	}
}


def prefSensor1() {
	log.debug "Doors chosen: " + doors
    
    //Set defaults
    def nextPage = ""
    def showInstall = true
    def titleText = ""
    
    //Determine if we have multiple doors and need to send to another page
    if (doors instanceof String){ //simulator seems to just make a single door a string. For that reason we have this weird check.
    	log.debug "Single door detected (string)."
        titleText = "Select Sensors for Door 1 (" + state.data[doors].name + ")"
    }
    else if (doors.size() == 1){
    	log.debug "Single door detected (array)."
        titleText = "Select Sensors for Door 1 (" + state.data[doors[0]].name + ")"
    }
    else{
    	log.debug "Multiple doors detected."
        nextPage = "prefSensor2"
        titleText = "OPTIONAL: Select Sensors for Door 1 (" + state.data[doors[0]].name + ")"
        showInstall = false;
    }    
    
    return dynamicPage(name: "prefSensor1",  title: "Optional Sensors and Push Buttons", nextPage:nextPage, install:showInstall, uninstall:true) {        
        section(titleText){			
			paragraph "Optional: If you have sensors on this door, select them below. A sensor allows the device type to know whether the door is open or closed, which helps the device function " + 
            	"as a switch you can turn on (to open) and off (to close)."                
            input(name: "door1Sensor", title: "Contact Sensor", type: "capability.contactSensor", required: false, multiple: false)
			input(name: "door1Acceleration", title: "Acceleration Sensor", type: "capability.accelerationSensor", required: false, multiple: false)
		}        
        section("Create separate on/off push buttons?"){			
			paragraph "Choose the option below to have separate additional On and Off push button devices created. This is recommened if you have no sensors but still want a way to open/close the " +
            "garage from SmartTiles and other interfaces like Google Home that can't function with the built-in open/close capability. See wiki for more details."           
            input "prefDoor1PushButtons", "bool", required: false, title: "Create on/off push buttons?"
		}
    }
}

def prefSensor2() {	   
    def nextPage = ""
    def showInstall = true
    def titleText = "Sensors for Door 2 (" + state.data[doors[1]].name + ")"
     
    if (doors.size() > 2){
    	nextPage = "prefSensor3"
        showInstall = false;
    }
    
    return dynamicPage(name: "prefSensor2",  title: "Optional Sensors and Push Buttons", nextPage:nextPage, install:showInstall, uninstall:true) {
        section(titleText){			
			input(name: "door2Sensor", title: "Contact Sensor", type: "capability.contactSensor", required: false, multiple: false)
			input(name: "door2Acceleration", title: "Acceleration Sensor", type: "capability.accelerationSensor", required: false, multiple: false)
		}
        section("Create separate on/off push buttons?"){			
			paragraph "Choose the option below to have extra on and off push button devices created. This is recommened if you have no sensors but still want a way to open/close the garage from SmartTiles."           
            input "prefDoor2PushButtons", "bool", required: false, title: "Create on/off push buttons?"
		}
    }
}

def prefSensor3() {	   
    def nextPage = ""
    def showInstall = true
    def titleText = "Sensors for Door 3 (" + state.data[doors[2]].name + ")"
     
    if (doors.size() > 3){
    	nextPage = "prefSensor4"
        showInstall = false;
    }
    
    return dynamicPage(name: "prefSensor3",  title: "Optional Sensors and Push Buttons", nextPage:nextPage, install:showInstall, uninstall:true) {
        section(titleText){			
			input(name: "door3Sensor", title: "Contact Sensor", type: "capability.contactSensor", required: false, multiple: false)
			input(name: "door3Acceleration", title: "Acceleration Sensor", type: "capability.accelerationSensor", required: false, multiple: false)
		}
        section("Create separate on/off push buttons?"){			
			paragraph "Choose the option below to have extra on and off push button devices created. This is recommened if you have no sensors but still want a way to open/close the garage from SmartTiles."           
            input "prefDoor3PushButtons", "bool", required: false, title: "Create on/off push buttons?"
		}
    }
}

def prefSensor4() {	   
	def titleText = "Contact Sensor for Door 4 (" + state.data[doors[3]].name + ")"
    return dynamicPage(name: "prefSensor4",  title: "Optional Sensors and Push Buttons", install:true, uninstall:true) {
        section(titleText){			
			input(name: "door4Sensor", title: "Contact Sensor", type: "capability.contactSensor", required: "false", multiple: "false")
			input(name: "door4Acceleration", title: "Acceleration Sensor", type: "capability.accelerationSensor", required: false, multiple: false)
		}
        section("Create separate on/off push buttons?"){			
			paragraph "Choose the option below to have extra on and off push button devices created. This is recommened if you have no sensors but still want a way to open/close the garage from SmartTiles."           
            input "prefDoor4PushButtons", "bool", required: false, title: "Create on/off push buttons?"
		}
    }
}

/* Initialization */
def installed() { 
	//initialize() 
}

def updated() { 
	log.debug "Updated..."
    unsubscribe()
	initialize()
}

def uninstalled() {}	

def initialize() {    
	log.debug "Initializing..."
    login()
    state.sensorMap = [:]
    
    // Get initial device status in state.data
	state.polling = [ last: 0, rescheduler: now() ]
	state.data = [:]
    
	// Create selected devices
	def doorsList = getDoorList()
	//def lightsList = getLightList()
    
    def firstDoor = doors[0]        
    //Handle single door (sometimes it's just a dumb string thanks to the simulator)
    if (doors instanceof String)
    firstDoor = doors   
    
        
    createChilDevices(firstDoor, door1Sensor, doorsList[firstDoor], prefDoor1PushButtons)
    if (doors[1]) createChilDevices(doors[1], door2Sensor, doorsList[doors[1]], prefDoor2PushButtons)
    if (doors[2]) createChilDevices(doors[2], door3Sensor, doorsList[doors[2]], prefDoor3PushButtons)
    if (doors[3]) createChilDevices(doors[3], door4Sensor, doorsList[doors[3]], prefDoor4PushButtons)    
    
    // Remove unselected devices
    getChildDevices().each{    	       

        //Modify DNI string for the extra pushbuttons to make sure they don't get deleted unintentionally
        def DNI = it?.deviceNetworkId
        DNI = DNI.replace("-OPEN", "")
        DNI = DNI.replace("-CLOSE", "")        

        if (!(DNI in doors)){
            log.debug "found device to delete: " + it
            try{
                	deleteChildDevice(it.deviceNetworkId)
            } catch (e){
                	sendPush("Warning: unable to delete door or button - " + it + "- you'll need to manually remove it.")
                    log.debug "Error trying to delete device " + it + " - " + e
                    log.debug "Device is likely in use in a Routine, or SmartApp (make sure and check SmarTiles!)."
            }
        }
    }
    
    //Create subscriptions
    if (door1Sensor)
        subscribe(door1Sensor, "contact", sensorHandler)
    if (door2Sensor)    	
        subscribe(door2Sensor, "contact", sensorHandler)
    if (door3Sensor)        
        subscribe(door3Sensor, "contact", sensorHandler)        
    if (door4Sensor)    	
        subscribe(door4Sensor, "contact", sensorHandler)
        
    if (door1Acceleration)
        subscribe(door1Acceleration, "acceleration", sensorHandler)    
    if (door2Acceleration)    	
        subscribe(door2Acceleration, "acceleration", sensorHandler)
    if (door3Acceleration)        
        subscribe(door3Acceleration, "acceleration", sensorHandler)        
    if (door4Acceleration)    	
        subscribe(door4Acceleration, "acceleration", sensorHandler)
        
    //Set initial values
    if (door1Sensor)
    	syncDoorsWithSensors()   
}

def createChilDevices(door, sensor, doorName, prefPushButtons){
	if (door){    	
        //Has door's child device already been created?
        def existingDev = getChildDevice(door)
        def existingType = existingDev?.typeName
        
        if (existingDev){        
        	log.debug "Child already exists for " + doorName + ". Sensor name is: " + sensor
            if ((!sensor) && existingType == "MyQ Garage Door Opener"){
            	log.debug "Type needs updating to non-sensor version"
                existingDev.deviceType = "MyQ Garage Door Opener-NoSensor"
            }
            
            if (sensor && existingType == "MyQ Garage Door Opener-NoSensor"){
            	log.debug "Type needs updating to sensor version"
                existingDev.deviceType = "MyQ Garage Door Opener"
            }            
        }
        else{
            log.debug "Creating child door device " + door
            if (sensor){
                addChildDevice("brbeaird", "MyQ Garage Door Opener", door, null, ["name": doorName]) 
            }
            else{
                addChildDevice("brbeaird", "MyQ Garage Door Opener-NoSensor", door, null, ["name": doorName]) 
            }
        }
        
        //Create push button devices
        if (prefPushButtons){
        	def existingOpenButtonDev = getChildDevice(door + "-OPEN")
            def existingCloseButtonDev = getChildDevice(door + "-CLOSE")
            if (!existingOpenButtonDev){
                def openButton = addChildDevice("smartthings", "Momentary Button Tile", door + "-OPEN", null, [name: doorName + "-OPEN", label: doorName + "-OPEN"])
                subscribe(openButton, "momentary.pushed", doorButtonOpenHandler)                
            }
            else{
            	subscribe(existingOpenButtonDev, "momentary.pushed", doorButtonOpenHandler)                
            }
            
            if (!existingCloseButtonDev){                
                def closeButton = addChildDevice("smartthings", "Momentary Button Tile", door + "-CLOSE", null, [name: doorName + "-CLOSE", label: doorName + "-CLOSE"])
                subscribe(closeButton, "momentary.pushed", doorButtonCloseHandler)
            }
            else{
                subscribe(existingCloseButtonDev, "momentary.pushed", doorButtonCloseHandler)                
            }
            
            
            
        	
        }
        
        //Cleanup defunct push button devices if no longer wanted
        else{
        	def pushButtonIDs = [door + "-OPEN", door + "-CLOSE"]
            log.debug "ID's to look for: " + pushButtonIDs
            def devsToDelete = getChildDevices().findAll { pushButtonIDs.contains(it.deviceNetworkId)}
            log.debug "button devices to delete: " + devsToDelete
			devsToDelete.each{
            	log.debug "deleting button: " + it
                try{
                	deleteChildDevice(it.deviceNetworkId)
                } catch (e){
                	sendPush("Warning: unable to delete virtual on/off push button - you'll need to manually remove it.")
                    log.debug "Error trying to delete button " + it + " - " + e
                    log.debug "Button  is likely in use in a Routine, or SmartApp (make sure and check SmarTiles!)."
                }
            	
            }
        } 	
    }
}


def syncDoorsWithSensors(child){	
    def firstDoor = doors[0]
        
    //Handle single door (sometimes it's just a dumb string thanks to the simulator)
    if (doors instanceof String)
    firstDoor = doors
    
    def doorDNI = null
    if (child) {								// refresh only the requesting door (makes things a bit more efficient if you have more than 1 door
    	doorDNI = child.device.deviceNetworkId
        switch (doorDNI) {
        	case firstDoor:
            	updateDoorStatus(firstDoor, door1Sensor, door1Acceleration, door1ThreeAxis, child)
                break
            case doors[1]:
            	updateDoorStatus(doors[1], door2Sensor, door2Acceleration, door2ThreeAxis, child)
                break
            case doors[2]:
            	updateDoorStatus(doors[2], door3Sensor, door3Acceleration, door3ThreeAxis, child)
                break
            case doors[3]:
            	updateDoorStatus(doors[3], door4Sensor, door4Acceleration, door4ThreeAxis, child)
     	}
    } else {           					// refresh ALL the doors
		if (firstDoor) updateDoorStatus(firstDoor, door1Sensor, door1Acceleration, door1ThreeAxis, null)
		if (doors[1]) updateDoorStatus(doors[1], door2Sensor, door2Acceleration, door2ThreeAxis, null)
		if (doors[2]) updateDoorStatus(doors[2], door3Sensor, door3Acceleration, door3ThreeAxis, null)
		if (doors[3]) updateDoorStatus(doors[3], door4Sensor, door4Acceleration, door4ThreeAxis, null)
    }
}

def updateDoorStatus(doorDNI, sensor, acceleration, threeAxis, child){
	
    //Get door to update and set the new value
    def doorToUpdate = getChildDevice(doorDNI)
    def doorName = state.data[doorDNI].name
    
    def value = "unknown"
    def moving = "unknown"
    def door = doorToUpdate.latestValue("door")
    
    if (acceleration) moving = acceleration.latestValue("acceleration")
    if (sensor) value = sensor.latestValue("contact")

    if (moving == "active") {
    	if (value == "open") {			
        	if (door != "opening") value = "closing" else value = "opening"  // if door is "open" or "waiting" change to "closing", else it must be "opening"
    	} else if (value == "closed") { 
        	if (door != "closing") 	value = "opening" else value = "closed"
    	}
    } else if (moving == "inactive") {
    	if (door == "closing") {
    		if (value == "open") { 	// just stopped but door is still open
    			value = "stopped"
        	}
        }
    }
    	
    doorToUpdate.updateDeviceStatus(value)
    doorToUpdate.updateDeviceSensor("${sensor} is ${sensor?.currentContact}")
    
    log.debug "Door: " + doorName + ": Updating with status - " + value + " -  from sensor " + sensor
    
    //Write to child log if this was initiated from one of the doors    
    if (child)
    	child.log("Door: " + doorName + ": Updating with status - " + value + " -  from sensor " + sensor)
 
     if (acceleration) {
     	doorToUpdate.updateDeviceMoving("${acceleration} is ${moving}")
        log.debug "Door: " + doorName + ": Updating with status - " + moving + " - from sensor " + acceleration
        if (child)
        	child.log("Door: " + doorName + ": Updating with status - " + moving + " - from sensor " + acceleration)
     }
    
    //Get latest activity timestamp for the sensor (data saved for up to a week)
    def eventsSinceYesterday = sensor.eventsSince(new Date() - 7)    
    def latestEvent = eventsSinceYesterday[0]?.date
    def timeStampLogText = "Door: " + doorName + ": Updating timestamp to: " + latestEvent + " -  from sensor " + sensor
    
    if (!latestEvent)	//If the door has been inactive for more than a week, timestamp data will be null. Keep current value in that case.
    	timeStampLogText = "Door: " + doorName + ": Null timestamp detected "  + " -  from sensor " + sensor + " . Keeping current value."
    else
    	doorToUpdate.updateDeviceLastActivity(latestEvent)    	
    	
    log.debug timeStampLogText    
    
    //Write to child log if this was initiated from one of the doors
    if (child)
    	child.log(timeStampLogText)
}

def refresh(child){	
    def door = child.device.deviceNetworkId
    def doorName = state.data[door].name
    child.log("refresh called from " + doorName + ' (' + door + ')')
    syncDoorsWithSensors(child)
}

def sensorHandler(evt) {    
    log.debug "Sensor change detected: Event name  " + evt.name + " value: " + evt.value   + " deviceID: " + evt.deviceId    
    
    switch (evt.deviceId) {
    	case door1Sensor.id:
        case door1Acceleration?.id:
            def firstDoor = doors[0]
			if (doors instanceof String) firstDoor = doors
        	updateDoorStatus(firstDoor, door1Sensor, door1Acceleration, door1ThreeAxis, null)
            break
    	case door2Sensor?.id:
        case door2Acceleration?.id:
        	updateDoorStatus(doors[1], door2Sensor, door2Acceleration, door2ThreeAxis, null)
            break    	
        case door3Sensor?.id:
        case door3Acceleration?.id:
        	updateDoorStatus(doors[2], door3Sensor, door3Acceleration, door3ThreeAxis, null)
            break        
    	case door4Sensor?.id:
        case door4Acceleration?.id:
        	updateDoorStatus(doors[3], door4Sensor, door4Acceleration, door4ThreeAxis, null)
            break
        default:
			syncDoorsWithSensors()
    }
}

def doorButtonOpenHandler(evt) {
    log.debug "Door open button push detected: Event name  " + evt.name + " value: " + evt.value   + " deviceID: " + evt.deviceId + " DNI: " + evt.getDevice().deviceNetworkId
    def doorDeviceDNI = evt.getDevice().deviceNetworkId
    doorDeviceDNI = doorDeviceDNI.replace("-OPEN", "")
    def doorDevice = getChildDevice(doorDeviceDNI)
    log.debug "Opening door."
    doorDevice.openPrep()
    sendCommand(doorDevice, "desireddoorstate", 1) 
}

def doorButtonCloseHandler(evt) {    
    log.debug "Door close button push detected: Event name  " + evt.name + " value: " + evt.value   + " deviceID: " + evt.deviceId + " DNI: " + evt.getDevice().deviceNetworkId
    def doorDeviceDNI = evt.getDevice().deviceNetworkId
    doorDeviceDNI = doorDeviceDNI.replace("-CLOSE", "")
	def doorDevice = getChildDevice(doorDeviceDNI)
    log.debug "Closing door."
    doorDevice.closePrep()
    sendCommand(doorDevice, "desireddoorstate", 0) 
}


def getSelectedDevices( settingsName ) { 
	def selectedDevices = [] 
	(!settings.get(settingsName))?:((settings.get(settingsName)?.getAt(0)?.size() > 1)  ? settings.get(settingsName)?.each { selectedDevices.add(it) } : selectedDevices.add(settings.get(settingsName))) 
	return selectedDevices 
} 

/* Access Management */
private forceLogin() {
	//Reset token and expiry
	state.session = [ brandID: 0, brandName: settings.brand, securityToken: null, expiration: 0 ]
	state.polling = [ last: 0, rescheduler: now() ]
	state.data = [:]
	return doLogin()
}

private login() { return (!(state.session.expiration > now())) ? doLogin() : true }

private doLogin() { 
	apiGet("/api/user/validate", [username: settings.username, password: settings.password] ) { response ->
		log.debug "got login response: " + response
        if (response.status == 200) {
			if (response.data.SecurityToken != null) {
				state.session.brandID = response.data.BrandId
				state.session.brandName = response.data.BrandName
				state.session.securityToken = response.data.SecurityToken
				state.session.expiration = now() + 150000
				return true
			} else {
				return false
			}
		} else {
			return false
		}
	} 	
}

// Listing all the garage doors you have in MyQ
private getDoorList() { 	    
	def deviceList = [:]
	apiGet("/api/v4/userdevicedetails/get", []) { response ->
		if (response.status == 200) {
        //log.debug "response data: " + response.data.Devices
        //sendAlert("response data: " + response.data.Devices)
			response.data.Devices.each { device ->
				// 2 = garage door, 5 = gate, 7 = MyQGarage(no gateway), 17 = Garage Door Opener WGDO
				if (device.MyQDeviceTypeName != 'VGDO' && (device.MyQDeviceTypeId == 2||device.MyQDeviceTypeId == 5||device.MyQDeviceTypeId == 7||device.MyQDeviceTypeId == 17)) {
					log.debug "Found door: " + device.MyQDeviceId
                    def dni = [ app.id, "GarageDoorOpener", device.MyQDeviceId ].join('|')
					def description = ''
                    def doorState = ''
                    def updatedTime = ''
                    device.Attributes.each { 
						
                        if (it.AttributeDisplayName=="desc")	//deviceList[dni] = it.Value                        	
                        {
                        	description = it.Value
                        }
                        	
						if (it.AttributeDisplayName=="doorstate") { 
                        	doorState = it.Value
                            updatedTime = it.UpdatedTime							
						}
					}
                    
                    //Ignore any doors with blank descriptions
                    if (description != ''){
                        log.debug "Storing door info: " + description + "type: " + device.MyQDeviceTypeId + " status: " + doorState +  " type: " + device.MyQDeviceTypeName
                        deviceList[dni] = description
                        state.data[dni] = [ status: doorState, lastAction: updatedTime, name: description ]
                    }
				}
			}
		}
	}    
	return deviceList
}

private getDeviceList() { 	    
	def deviceList = []
	apiGet("/api/v4/userdevicedetails/get", []) { response ->
		if (response.status == 200) {
			response.data.Devices.each { device ->
				log.debug "MyQDeviceTypeId : " + device.MyQDeviceTypeId.toString()
				if (!(device.MyQDeviceTypeId == 1||device.MyQDeviceTypeId == 2||device.MyQDeviceTypeId == 3||device.MyQDeviceTypeId == 5||device.MyQDeviceTypeId == 7)) {					
                    device.Attributes.each { 
						def description = ''
                        def doorState = ''
                        def updatedTime = ''
                        if (it.AttributeDisplayName=="desc")	//deviceList[dni] = it.Value
                        	description = it.Value
						
                        //Ignore any doors with blank descriptions
                        if (description != ''){
                        	log.debug "found device: " + description
                        	deviceList.add( device.MyQDeviceTypeId.toString() + "|" + device.TypeID )
                        }
					}
				}
			}
		}
	}    
	return deviceList
}

/* api connection */
// get URL 
private getApiURL() {
	if (settings.brand == "Craftsman") {
		return "https://craftexternal.myqdevice.com"
	} else {
		return "https://myqexternal.myqdevice.com"
	}
}

private getApiAppID() {
	if (settings.brand == "Craftsman") {
		return "QH5AzY8MurrilYsbcG1f6eMTffMCm3cIEyZaSdK/TD/8SvlKAWUAmodIqa5VqVAs"
	} else {
		return "JVM/G9Nwih5BwKgNCjLxiFUQxQijAebyyg8QUHr7JOrP+tuPb8iHfRHKwTmDzHOu"
	}
}
	
// HTTP GET call
private apiGet(apiPath, apiQuery = [], callback = {}) {	
	// set up query
	apiQuery = [ appId: getApiAppID() ] + apiQuery
	if (state.session.securityToken) { apiQuery = apiQuery + [SecurityToken: state.session.securityToken ] }
       
	try {
		httpGet([ uri: getApiURL(), path: apiPath, query: apiQuery ]) { response -> callback(response) }
	}	catch (SocketException e)	{
		//sendAlert("API Error: $e")
        log.debug "API Error: $e"
	}
}

// HTTP PUT call
private apiPut(apiPath, apiBody = [], callback = {}) {    
	// set up body
	apiBody = [ ApplicationId: getApiAppID() ] + apiBody
	if (state.session.securityToken) { apiBody = apiBody + [SecurityToken: state.session.securityToken ] }
    
	// set up query
	def apiQuery = [ appId: getApiAppID() ]
	if (state.session.securityToken) { apiQuery = apiQuery + [SecurityToken: state.session.securityToken ] }
    
	try {
		httpPut([ uri: getApiURL(), path: apiPath, contentType: "application/json; charset=utf-8", body: apiBody, query: apiQuery ]) { response -> callback(response) }
	} catch (SocketException e)	{
		log.debug "API Error: $e"
	}
}

// Get Device ID
def getChildDeviceID(child) {
	return child.device.deviceNetworkId.split("\\|")[2]
}

// Get single device status
def getDeviceStatus(child) {
	return state.data[child.device.deviceNetworkId].status
}

// Get single device last activity
def getDeviceLastActivity(child) {
	return state.data[child.device.deviceNetworkId].lastAction.toLong()
}

// Send command to start or stop
def sendCommand(child, attributeName, attributeValue) {
	if (login()) {	    	
		//Send command
		apiPut("/api/v4/deviceattribute/putdeviceattribute", [ MyQDeviceId: getChildDeviceID(child), AttributeName: attributeName, AttributeValue: attributeValue ]) 
        
        if ((attributeName == "desireddoorstate") && (attributeValue == 0)) {		// if we are closing, check if we have an Acceleration sensor, if so, "waiting" until it moves
            def firstDoor = doors[0]
    		if (doors instanceof String) firstDoor = doors
        	def doorDNI = child.device.deviceNetworkId
        	switch (doorDNI) {
        		case firstDoor:
                	if (door1Sensor){if (door1Acceleration) child.updateDeviceStatus("waiting") else child.updateDeviceStatus("opening")}
                	break
            	case doors[1]:
            		if (door2Sensor){if (door2Acceleration) child.updateDeviceStatus("waiting") else child.updateDeviceStatus("opening")}
                	break
            	case doors[2]:
            		if (door3Sensor){if (door3Acceleration) child.updateDeviceStatus("waiting") else child.updateDeviceStatus("opening")}
                	break
            	case doors[3]:
            		if (door4Sensor){if (door4Acceleration) child.updateDeviceStatus("waiting") else child.updateDeviceStatus("opening")}
        			break
            }
        }      
		return true
	} 
}