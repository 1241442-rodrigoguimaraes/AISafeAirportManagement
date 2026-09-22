flight TP200 {
    type:     regular
    route:    TP200
    date:     2025-06-01
    time:     08:00
    aircraft: CS-TUB

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
            mode:           climb
            start: { latitude: 41.262891  longitude: -8.68522  altitude: 69   m }
            end:   { latitude: 40.472779  longitude: -3.560833 altitude: 9000 m }
            altitude_slots: [3000, 6000, 9000]
            width:          50 m
            wind:           270 deg 15 m/s
        }
    }

    leg {
        departure: LIS
        arrival:   BCN

        fuel {
            quantity: 3100
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
            start: { latitude: 38.774167  longitude: -9.134167  altitude: 114  m }
            end:   { latitude: 41.297077  longitude: 2.078463   altitude: 9000 m }
            altitude_slots: [3000, 6000, 9000]
            width:          50 m
            wind:           090 deg 10 m/s
        }
    }
}
