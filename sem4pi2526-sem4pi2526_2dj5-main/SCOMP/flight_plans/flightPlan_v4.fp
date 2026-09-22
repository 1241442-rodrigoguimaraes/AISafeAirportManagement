flight AA004 {
    type:     regular
    route:    AA004
    date:     2026-08-05
    time:     14:30
    aircraft: VO-VLO

    leg {
        departure: LAX
        arrival:   JFK

        fuel {
            quantity: 195000
            unit:     kg
        }

        profile {
            climb {
                altitude { quantity: 0     unit: m }
                speed    { quantity: 250   unit: knots }
                altitude { quantity: 10600 unit: m }
                speed    { quantity: 300   unit: knots }
            }
            cruise {
                speed { quantity: 475 unit: knots }
            }
            descend {
                altitude     { quantity: 10600 unit: m }
                speed        { quantity: 290   unit: knots }
                rate_descent { quantity: -12   unit: m/s }
                altitude     { quantity: 0     unit: m }
                speed        { quantity: 140   unit: knots }
                rate_descent { quantity: -4    unit: m/s }
            }
        }

        segment {
            mode:           climb
            start: { latitude: 33.9416   longitude: -118.4085 altitude: 38    m }
            end:   { latitude: 35.2000   longitude: -112.5000 altitude: 10058 m }
            altitude_slots: [3000, 6000, 9000]
            width:          55 m
            wind:           260 deg 12 m/s
        }

        segment {
            mode:           cruise
            start: { latitude: 35.2000   longitude: -112.5000 altitude: 10058 m }
            end:   { latitude: 40.0000   longitude: -78.5000  altitude: 10668 m }
            altitude_slots: [9000, 10000, 11000]
            width:          55 m
            wind:           280 deg 40 m/s
        }

        segment {
            mode:           descend
            start: { latitude: 40.0000   longitude: -78.5000  altitude: 10668 m }
            end:   { latitude: 40.6398   longitude: -73.7789  altitude: 4     m }
            altitude_slots: [9000, 6000, 3000]
            width:          55 m
            wind:           310 deg 15 m/s
        }
    }
}