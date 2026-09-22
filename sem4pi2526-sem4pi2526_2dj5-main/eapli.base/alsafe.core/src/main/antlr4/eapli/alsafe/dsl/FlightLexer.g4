lexer grammar FlightLexer;

fragment A: [aA]; fragment B: [bB]; fragment C: [cC];
fragment D: [dD]; fragment E: [eE]; fragment F: [fF];
fragment G: [gG]; fragment H: [hH]; fragment I: [iI];
fragment J: [jJ]; fragment K: [kK]; fragment L: [lL];
fragment M: [mM]; fragment N: [nN]; fragment O: [oO];
fragment P: [pP]; fragment Q: [qQ]; fragment R: [rR];
fragment S: [sS]; fragment T: [tT]; fragment U: [uU];
fragment V: [vV]; fragment W: [wW]; fragment X: [xX];
fragment Y: [yY]; fragment Z: [zZ];

FLIGHT         : F L I G H T ;
LEG            : L E G ;
TYPE           : T Y P E ;
DEPARTURE      : D E P A R T U R E ;
ARRIVAL        : A R R I V A L ;
ROUTE          : R O U T E ;
SEGMENT        : S E G M E N T ;
FUEL           : F U E L ;
ALTITUDE       : A L T I T U D E ;
ALTITUDE_SLOTS : A L T I T U D E [_] S L O T S ;
PROFILE        : P R O F I L E ;
CLIMB          : C L I M B ;
DESCEND        : D E S C E N D ;
CRUISE         : C R U I S E ;
SPEED          : S P E E D ;
RATEDESCENT    : R A T E [_] D E S C E N T ;
MODE           : M O D E ;
START          : S T A R T ;
END_KW         : E N D ;
LATITUDE       : L A T I T U D E ;
LONGITUDE      : L O N G I T U D E ;
QUANTITY       : Q U A N T I T Y ;
WIDTH          : W I D T H ;
WIND           : W I N D ;
UNIT           : U N I T ;
DATE_KW        : D A T E ;
TIME_KW        : T I M E ;
AIRCRAFT       : A I R C R A F T ;

REGULAR : R E G U L A R ;
CHARTER : C H A R T E R ;

DATE_LIT : [0-9][0-9][0-9][0-9] '-' [0-9][0-9] '-' [0-9][0-9] ;
TIME_LIT : ([0-1][0-9] | [2][0-3]) ':' [0-5][0-9] ;

NEG_NUMBER : '-' [0-9]+ ('.' [0-9]+)? ;
NUMBER     : [0-9]+ ('.' [0-9]+)? ;

UNIT_KG    : K G ;
UNIT_L     : L ;
UNIT_MS    : M '/' S ;
UNIT_M     : M ;
UNIT_DEG   : D E G ;
UNIT_KNOTS : K N O T S ;

IATA_CODE         : [A-Z][A-Z][A-Z] ;
FLIGHT_DESIGNATOR : [a-zA-Z][a-zA-Z][0-9][0-9]?[0-9]?[0-9]?[a-zA-Z]? ;
AIRCRAFT_REG      : [A-Z][A-Z] '-' [A-Z][A-Z0-9]+ ;
IDCODE            : [a-zA-Z][a-zA-Z0-9_]* ;

LBRACE : '{' ;
RBRACE : '}' ;
LBRACK : '[' ;
RBRACK : ']' ;
COLON  : ':' ;
COMMA  : ',' ;

COMMENT : '//' ~[\r\n]* -> skip ;
WS      : [ \t\r\n]+ -> skip ;