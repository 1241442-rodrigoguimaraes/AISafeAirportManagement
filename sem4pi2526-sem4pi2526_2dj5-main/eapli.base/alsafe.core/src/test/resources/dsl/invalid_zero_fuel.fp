flight TP202 {
    type:     regular
    route:    TP202
    date:     2025-06-03
    time:     11:00
    aircraft: CS-TUD

    leg {
        departure: OPO
        arrival:   MAD

        fuel {
            quantity: 0
            unit:     kg
        }

        profile {
            climb {
                altitude { quantity: 0    unit: m }
                speed    { quantity: 210  unit: knots }
                altitude { quantity: 9000 unit: m }
                speed    { quantity: 300  unit: knots }
            }
            cruise {
                speed { quantity: 460 unit: knots }
            }
            descend {
                altitude     { quantity: 9000 unit: m }
                speed        { quantity: 300  unit: knots }
                rate_descent { quantity: -10  unit: m/s }
                altitude     { quantity: 0    unit: m }
                speed        { quantity: 140  unit: knots }
                rate_descent { quantity: -5   unit: m/s }
            }
        }

        segment {
            mode:           climb
            start: { latitude: 41.262891  longitude: -8.68522  altitude: 69   m }
            end:   { latitude: 40.472779  longitude: -3.560833 altitude: 9000 m }
            altitude_slots: [3000, 6000, 9000]
            width:          50 m
            wind:           270 deg 15 m/s
        }
    }
}
