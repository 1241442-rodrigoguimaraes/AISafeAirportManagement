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
            quantity: 500000
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

        segment {
            mode: cruise
            start: { latitude: 41.262891 longitude: -8.68522 altitude: 9249 m }
            end:   { latitude: 42.0       longitude: -8.01     altitude: 9249 m }
            altitude_slots: [3000, 6000, 9000]
            width: 50 m
            wind: 270 deg 15 m/s
        }

        segment {
            mode: descend
            start: { latitude: 42.0       longitude: -8.01     altitude: 9249 m }
            end:   { latitude: 41.0       longitude: -8.5      altitude: 0 m }
            altitude_slots: [9000, 6000, 3000]
            width: 50 m
            wind: 270 deg 10 m/s
        }
    }
}