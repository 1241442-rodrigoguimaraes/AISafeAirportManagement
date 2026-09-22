flight TP123 {
    type:     regular
    route:    TP123
    date:     2025-05-01
    time:     09:00
    aircraft: CS-TUA

    leg {
        departure: OPO
        arrival:   MAD

        fuel {
            quantity: 4200
            unit:     kg
        }

        profile {
            climb {
                altitude { quantity: 0     unit: m }
                speed    { quantity: 210   unit: knots }
                altitude { quantity: 12000 unit: m }
                speed    { quantity: 300   unit: knots }
            }
            cruise {
                speed { quantity: 460 unit: knots }
            }
            descend {
                altitude     { quantity: 12000 unit: m }
                speed        { quantity: 300   unit: knots }
                rate_descent { quantity: -10   unit: m/s }
                altitude     { quantity: 0     unit: m }
                speed        { quantity: 140   unit: knots }
                rate_descent { quantity: -5    unit: m/s }
            }
        }

        segment {
            mode:           climb
            start: { latitude: 41.262891  longitude: -8.68522  altitude: 69   m }
            end:   { latitude: 42.0       longitude: -8.01     altitude: 9249 m }
            altitude_slots: [3000, 6000, 9000]
            width:          50 m
            wind:           270 deg 15 m/s
        }
    }
}

flight EK312 {
    type:     charter
    route:    EK312
    date:     2025-06-15
    time:     14:30
    aircraft: CS-TUB

    leg {
        departure: LIS
        arrival:   BCN

        fuel {
            quantity: 3500
            unit:     l
        }

        profile {
            climb {
                altitude { quantity: 0     unit: m }
                speed    { quantity: 200   unit: knots }
            }
            cruise {
                speed { quantity: 420 unit: knots }
            }
            descend {
                altitude     { quantity: 8000 unit: m }
                speed        { quantity: 250  unit: knots }
                rate_descent { quantity: -8   unit: m/s }
            }
        }

        segment {
            mode:           climb
            start: { latitude: 38.774167  longitude: -9.134722 altitude: 114  m }
            end:   { latitude: 39.5       longitude: -8.0       altitude: 7500 m }
            altitude_slots: [2000, 5000]
            width:          40 m
            wind:           200 deg 12 m/s
        }
    }
}
