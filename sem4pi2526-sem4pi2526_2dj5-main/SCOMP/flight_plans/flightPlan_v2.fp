flight EK312 {
    type:     regular
    route:    EK312
    date:     2026-06-15
    time:     08:00
    aircraft: AI-EEQ

    leg {
        departure: DXB
        arrival:   HND

        fuel {
            quantity: 5000000
            unit:     kg
        }

        profile {
            climb {
                altitude { quantity: 0     unit: m }
                speed    { quantity: 250   unit: knots }
                altitude { quantity: 11300 unit: m }
                speed    { quantity: 300   unit: knots }
            }
            cruise {
                speed { quantity: 485 unit: knots }
            }
            descend {
                altitude     { quantity: 11300 unit: m }
                speed        { quantity: 290   unit: knots }
                rate_descent { quantity: -11   unit: m/s }
                altitude     { quantity: 0     unit: m }
                speed        { quantity: 142   unit: knots }
                rate_descent { quantity: -5    unit: m/s }
            }
        }

        segment {
            mode:           climb
            start: { latitude: 25.2532   longitude: 55.3657   altitude: 19    m }
            end:   { latitude: 28.0000   longitude: 62.0000   altitude: 10668 m }
            altitude_slots: [4000, 7000, 10000]
            width:          60 m
            wind:           120 deg 10 m/s
        }

        segment {
            mode:           cruise
            start: { latitude: 28.0000   longitude: 62.0000   altitude: 10668 m }
            end:   { latitude: 33.5000   longitude: 135.0000  altitude: 11277 m }
            altitude_slots: [10000, 11000]
            width:          60 m
            wind:           270 deg 55 m/s
        }

        segment {
            mode:           descend
            start: { latitude: 33.5000   longitude: 135.0000  altitude: 11277 m }
            end:   { latitude: 35.5494   longitude: 139.7798  altitude: 11    m }
            altitude_slots: [9000, 6000, 3000]
            width:          60 m
            wind:           020 deg 15 m/s
        }
    }
}