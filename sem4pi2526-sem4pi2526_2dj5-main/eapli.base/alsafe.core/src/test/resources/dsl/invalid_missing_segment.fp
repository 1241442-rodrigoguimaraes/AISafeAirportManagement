flight TP123 {
    type:     regular
    route:    TP123
    date:     2025-05-01
    time:     09:00
    aircraft: CS-TUA
    leg {
        departure: OPO
        arrival:   MAD
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
    }
}