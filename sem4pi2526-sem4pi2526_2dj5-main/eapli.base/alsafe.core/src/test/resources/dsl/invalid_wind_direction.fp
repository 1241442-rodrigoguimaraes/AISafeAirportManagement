flight TP301 {
    type:     regular
    route:    TP301
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
            wind:           361 deg 15 m/s
        }
    }
}
