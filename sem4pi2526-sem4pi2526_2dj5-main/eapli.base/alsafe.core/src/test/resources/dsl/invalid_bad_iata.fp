flight TP123 {
    type:     regular
    route:    TP123
    date:     2025-05-01
    time:     09:00
    aircraft: CS-TUA
    leg {
        departure: LPPR
        arrival:   LEMD
        fuel { quantity: 4200 unit: kg }
        profile {
            climb {
                altitude { quantity: 0 unit: m }
                speed    { quantity: 210 unit: knots }
            }
            cruise { speed { quantity: 460 unit: knots } }
            descend {
                altitude     { quantity: 0 unit: m }
                speed        { quantity: 140 unit: knots }
                rate_descent { quantity: -5 unit: m/s }
            }
        }
        segment {
            mode: climb
            start: { latitude: 41.26 longitude: -8.68 altitude: 69 m }
            end:   { latitude: 42.0  longitude: -8.01 altitude: 9249 m }
            altitude_slots: [3000]
            width: 50 m
            wind:  270 deg 15 m/s
        }
    }
}