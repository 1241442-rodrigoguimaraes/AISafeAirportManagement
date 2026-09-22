flight PT22B {
    type:     regular
    route:    PA2345
    date:     2026-09-02
    time:     13:30
    aircraft: CM-TPW

    leg {
        departure: LIS
        arrival:   POR

        fuel {
            quantity: 12000
            unit:     kg
        }

        profile {
            climb {
                altitude { quantity: 0     unit: m }
                speed    { quantity: 250   unit: knots }
                altitude { quantity: 7315  unit: m }
                speed    { quantity: 280   unit: knots }
            }
            cruise {
                speed { quantity: 410 unit: knots }
            }
            descend {
                altitude     { quantity: 7315  unit: m }
                speed        { quantity: 260   unit: knots }
                rate_descent { quantity: -9    unit: m/s }
                altitude     { quantity: 0     unit: m }
                speed        { quantity: 135   unit: knots }
                rate_descent { quantity: -3    unit: m/s }
            }
        }

        segment {
            mode:           climb
            start: { latitude: 38.7813   longitude: -9.1359   altitude: 114   m }
            end:   { latitude: 39.4000   longitude: -8.9000   altitude: 7000  m }
            altitude_slots: [2000, 4000, 6000]
            width:          30 m
            wind:           320 deg 10 m/s
        }

        segment {
            mode:           cruise
            start: { latitude: 39.4000   longitude: -8.9000   altitude: 7315  m }
            end:   { latitude: 40.7500   longitude: -8.6500   altitude: 7315  m }
            altitude_slots: [7000, 7500, 8000]
            width:          30 m
            wind:           330 deg 15 m/s
        }

        segment {
            mode:           descend
            start: { latitude: 40.7500   longitude: -8.6500   altitude: 7315  m }
            end:   { latitude: 41.2421   longitude: -8.6785   altitude: 69    m }
            altitude_slots: [5000, 3000, 1500]
            width:          30 m
            wind:           310 deg 12 m/s
        }
    }
}