flight TP123 {
    type: regular
    route: PA2345
    date: 2026-07-02
    time: 22:14
    aircraft: CM-TPW

    leg {
        departure: LIS
        arrival: POR

        fuel {
            quantity: 20000
            unit: kg
        }

        profile {
            climb {
                altitude { quantity: 0 unit: m }
                speed { quantity: 210 unit: knots }
                altitude { quantity: 12000 unit: m }
                speed { quantity: 300 unit: knots }
            }
            cruise {
                speed { quantity: 460 unit: knots }
            }
            descend {
                altitude { quantity: 12000 unit: m }
                speed { quantity: 300 unit: knots }
                rate_descent { quantity: -10 unit: m/s }
                altitude { quantity: 0 unit: m }
                speed { quantity: 140 unit: knots }
                rate_descent { quantity: -5 unit: m/s }
            }
        }

        segment {
            mode: climb
            start: { latitude: 38.7742 longitude: -9.1342 altitude: 114 m }
            end: { latitude: 39.5000 longitude: -8.5000 altitude: 9000 m }
            altitude_slots: [3000, 6000, 9000]
            width: 50 m
            wind: 270 deg 15 m/s
        }

        segment {
            mode: cruise
            start: { latitude: 39.5000 longitude: -8.5000 altitude: 9000 m }
            end: { latitude: 41.0000 longitude: -8.7000 altitude: 9000 m }
            altitude_slots: [3000, 6000, 9000]
            width: 50 m
            wind: 270 deg 15 m/s
        }

        segment {
            mode: descend
            start: { latitude: 41.0000 longitude: -8.7000 altitude: 9000 m }
            end: { latitude: 41.2421 longitude: -8.6781 altitude: 69 m }
            altitude_slots: [9000, 6000, 3000]
            width: 50 m
            wind: 270 deg 10 m/s
        }
    }
}
