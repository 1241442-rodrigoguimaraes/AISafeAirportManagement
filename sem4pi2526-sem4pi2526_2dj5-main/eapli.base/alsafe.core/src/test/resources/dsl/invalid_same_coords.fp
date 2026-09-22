flight TP201 {
    type:     regular
    route:    TP201
    date:     2025-06-02
    time:     10:00
    aircraft: CS-TUC

    leg {
        departure: OPO
        arrival:   MAD

        fuel {
            quantity: 4200
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
            mode:           cruise
            start: { latitude: 41.262891  longitude: -8.68522  altitude: 9000 m }
            end:   { latitude: 41.262891  longitude: -8.68522  altitude: 9000 m }
            altitude_slots: [9000]
            width:          50 m
            wind:           270 deg 15 m/s
        }
    }
}
