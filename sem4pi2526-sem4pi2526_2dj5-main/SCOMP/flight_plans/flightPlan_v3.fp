flight QF143 {
    type:     regular
    route:    QF143
    date:     2026-07-20
    time:     11:15
    aircraft: VH-VZO

    leg {
        departure: SYD
        arrival:   AKL

        fuel {
            quantity: 112000
            unit:     kg
        }

        profile {
            climb {
                altitude { quantity: 0     unit: m }
                speed    { quantity: 230   unit: knots }
                altitude { quantity: 11900 unit: m }
                speed    { quantity: 290   unit: knots }
            }
            cruise {
                speed { quantity: 460 unit: knots }
            }
            descend {
                altitude     { quantity: 11900 unit: m }
                speed        { quantity: 280   unit: knots }
                rate_descent { quantity: -10   unit: m/s }
                altitude     { quantity: 0     unit: m }
                speed        { quantity: 138   unit: knots }
                rate_descent { quantity: -4    unit: m/s }
            }
        }

        segment {
            mode:           climb
            start: { latitude: -33.9461  longitude: 151.1772  altitude: 6     m }
            end:   { latitude: -34.5000  longitude: 155.0000  altitude: 10972 m }
            altitude_slots: [3000, 6000, 9000]
            width:          50 m
            wind:           190 deg 25 m/s
        }

        segment {
            mode:           cruise
            start: { latitude: -34.5000  longitude: 155.0000  altitude: 10972 m }
            end:   { latitude: -36.5000  longitude: 172.0000  altitude: 11887 m }
            altitude_slots: [11000, 12000]
            width:          50 m
            wind:           220 deg 30 m/s
        }

        segment {
            mode:           descend
            start: { latitude: -36.5000  longitude: 172.0000  altitude: 11887 m }
            end:   { latitude: -37.0081  longitude: 174.7917  altitude: 7     m }
            altitude_slots: [9000, 6000, 3000]
            width:          50 m
            wind:           260 deg 18 m/s
        }
    }
}