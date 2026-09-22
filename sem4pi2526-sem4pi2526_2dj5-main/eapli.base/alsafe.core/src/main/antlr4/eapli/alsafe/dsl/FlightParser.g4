parser grammar FlightParser;

options { tokenVocab = FlightLexer; }

start : flightBlock+ EOF ;

flightBlock
    : FLIGHT FLIGHT_DESIGNATOR LBRACE
        TYPE     COLON flightType
        ROUTE    COLON FLIGHT_DESIGNATOR
        DATE_KW  COLON DATE_LIT
        TIME_KW  COLON TIME_LIT
        AIRCRAFT COLON AIRCRAFT_REG
        legBlock+
      RBRACE
    ;

flightType : REGULAR | CHARTER ;

legBlock
    : LEG LBRACE
        DEPARTURE COLON airportCode
        ARRIVAL   COLON airportCode
        fuelInfo
        flightProfile
        segmentBlock+
      RBRACE
    ;

airportCode : IATA_CODE ;

fuelInfo
    : FUEL LBRACE
        QUANTITY COLON NUMBER
        UNIT     COLON fuelUnit
      RBRACE
    ;

fuelUnit : UNIT_KG | UNIT_L ;

flightProfile
    : PROFILE LBRACE
        climbProfile
        cruiseProfile
        descendProfile
      RBRACE
    ;

climbProfile
    : CLIMB LBRACE
        climbEntry+
      RBRACE
    ;

climbEntry : altitudeValue speedValue ;

cruiseProfile
    : CRUISE LBRACE
        speedValue
      RBRACE
    ;

descendProfile
    : DESCEND LBRACE
        descendEntry+
      RBRACE
    ;

descendEntry : altitudeValue speedValue rateDescentValue ;

altitudeValue
    : ALTITUDE LBRACE
        QUANTITY COLON NUMBER
        UNIT     COLON UNIT_M
      RBRACE
    ;

speedValue
    : SPEED LBRACE
        QUANTITY COLON NUMBER
        UNIT     COLON speedUnit
      RBRACE
    ;

rateDescentValue
    : RATEDESCENT LBRACE
        QUANTITY COLON signedNumber
        UNIT     COLON UNIT_MS
      RBRACE
    ;

signedNumber : NEG_NUMBER | NUMBER ;

speedUnit : UNIT_KNOTS | UNIT_MS ;

segmentBlock
    : SEGMENT LBRACE
        MODE           COLON segmentMode
        START          COLON coords3d
        END_KW         COLON coords3d
        ALTITUDE_SLOTS COLON altitudeSlotList
        WIDTH          COLON NUMBER UNIT_M
        WIND           COLON NUMBER UNIT_DEG NUMBER UNIT_MS
      RBRACE
    ;

segmentMode : CLIMB | CRUISE | DESCEND ;

coords3d
    : LBRACE
        LATITUDE  COLON signedNumber
        LONGITUDE COLON signedNumber
        ALTITUDE  COLON NUMBER UNIT_M
      RBRACE
    ;

altitudeSlotList
    : LBRACK NUMBER (COMMA NUMBER)* RBRACK ;