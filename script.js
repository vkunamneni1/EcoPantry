document.addEventListener('DOMContentLoaded', () => {

    // --- SHARED DATA & UTILITIES (using localStorage for persistence) ---

    // Initialize data if it doesn't exist in localStorage
    const initializeData = () => {
        if (!localStorage.getItem('foodItems')) {
            const initialFoodItems = [
                { id: 1, name: 'Organic Bananas', category: 'Produce', expiryDate: '2025-06-05', price: 2.50, addedDate: '2025-05-28' },
                { id: 2, name: 'Greek Yogurt', category: 'Dairy', expiryDate: '2025-06-15', price: 4.00, addedDate: '2025-05-28' },
                { id: 3, name: 'Sourdough Bread', category: 'Bakery', expiryDate: '2025-06-03', price: 5.50, addedDate: '2025-05-26' },
                { id: 4, name: 'Chicken Breast', category: 'Meat', expiryDate: '2025-06-08', price: 12.75, addedDate: '2025-05-29' },
                { id: 5, name: 'Spinach', category: 'Produce', expiryDate: '2025-06-07', price: 3.00, addedDate: '2025-05-29' },
            ];
            localStorage.setItem('foodItems', JSON.stringify(initialFoodItems));
        }
        if (!localStorage.getItem('userStats')) {
            const stats = { name: 'Alex', points: 1247, level: 'Eco-Sprout' };
            localStorage.setItem('userStats', JSON.stringify(stats));
        }
    };
    
    // Utility to get and save data
    const getData = (key) => JSON.parse(localStorage.getItem(key)) || [];
    const saveData = (key, data) => localStorage.setItem(key, JSON.stringify(data));

    const getDaysUntilExpiry = (expiryDate) => {
        const today = new Date();
        const expiry = new Date(expiryDate);
        today.setHours(0, 0, 0, 0);
        expiry.setHours(0, 0, 0, 0);
        return Math.round((expiry - today) / (1000 * 60 * 60 * 24));
    };
    
    const getStatus = (days) => {
        if (days < 0) return { text: 'Expired', class: 'critical' };
        if (days <= 3) return { text: `${days}d left`, class: 'critical' };
        if (days <= 7) return { text: `${days}d left`, class: 'warning' };
        return { text: 'Fresh', class: 'good' };
    };

    // --- PAGE-SPECIFIC LOGIC ---

    const currentPageId = document.body.id;

    // --- DASHBOARD LOGIC ---
    if (currentPageId === 'page-dashboard') {
        const userStats = getData('userStats');
        
        // 1. Set dynamic greeting
        const hour = new Date().getHours();
        const greeting = hour < 12 ? 'Good Morning' : hour < 18 ? 'Good Afternoon' : 'Good Evening';
        document.getElementById('welcome-greeting').textContent = `${greeting}, ${userStats.name}!`;

        // 2. Set daily tip
        const tips = [
            "Store herbs like cilantro with their stems in a glass of water.",
            "Keep bananas separate from other fruits to prevent them from ripening too fast.",
            "Revive wilted greens by soaking them in ice water for 30 minutes.",
            "Use citrus peels to make a natural all-purpose cleaner."
        ];
        document.getElementById('daily-tip').textContent = tips[new Date().getDate() % tips.length];

        // 3. Render expiring items
        const foodItems = getData('foodItems');
        const expiringGrid = document.getElementById('expiring-soon-grid');
        const expiringItems = foodItems
            .filter(item => getDaysUntilExpiry(item.expiryDate) <= 7 && getDaysUntilExpiry(item.expiryDate) >= 0)
            .sort((a,b) => getDaysUntilExpiry(a.expiryDate) - getDaysUntilExpiry(b.expiryDate))
            .slice(0, 4); // Show top 4

        if (expiringItems.length > 0) {
            expiringItems.forEach(item => {
                const days = getDaysUntilExpiry(item.expiryDate);
                const status = getStatus(days);
                const totalLife = getDaysUntilExpiry(item.expiryDate) - getDaysUntilExpiry(item.addedDate);
                const progress = Math.max(0, (days / 7)) * 100;
                
                const card = document.createElement('div');
                card.className = `expiring-item-card ${status.class}`;
                card.innerHTML = `
                    <h4>${item.name}</h4>
                    <p>${status.text}</p>
                    <div class="expiry-progress" title="${status.text}">
                        <div class="expiry-progress-bar" style="width:${progress}%; background-color: var(--${status.class}-color);"></div>
                    </div>
                    <div class="actions">
                        <a href="recipes.html" class="btn btn-primary">Recipes</a>
                        <button class="btn">Used It</button>
                    </div>
                `;
                expiringGrid.appendChild(card);
            });
        } else {
            expiringGrid.innerHTML = '<p>Nothing is expiring in the next 7 days. Your pantry is in great shape!</p>';
        }
    }

    // --- INVENTORY PAGE LOGIC ---
    // (You would create inventory.html and add this logic)
    // if (currentPageId === 'page-inventory') {
    //   ... render full inventory grid with filters and add/edit modals ...
    // }

    // --- RECIPES PAGE LOGIC ---
    // (You would create recipes.html and add this logic)
    // if (currentPageId === 'page-recipes') {
    //   ... render recipe cards, filtering by available ingredients ...
    // }
    
    // --- And so on for other pages ---


    // --- INITIALIZE APP ---
    initializeData();
});