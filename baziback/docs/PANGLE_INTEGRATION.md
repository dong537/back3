# Pangle Integration

This project already includes three ad entry points:

- Splash ad on app startup
- Carousel ad on the home page
- Reward ad flow in the credits area

## Environment variables

Create a `.env` file in the project root:

```env
VITE_PANGLE_SLOT_ID=your_general_slot_id
VITE_PANGLE_CAROUSEL_SLOT_ID=your_home_carousel_slot_id
VITE_PANGLE_SPLASH_SLOT_ID=your_splash_slot_id
```

Slot fallback rules:

- Reward ad: `VITE_PANGLE_SLOT_ID`, then `VITE_PANGLE_CAROUSEL_SLOT_ID`
- Carousel ad: `VITE_PANGLE_CAROUSEL_SLOT_ID`, then `VITE_PANGLE_SLOT_ID`
- Splash ad: `VITE_PANGLE_SPLASH_SLOT_ID`, then `VITE_PANGLE_SLOT_ID`

## Frontend entry points

- Splash ad: `src-frontend/App.jsx`
- Home carousel ad: `src-frontend/pages/Home.jsx`
- Reward ad popup: `src-frontend/components/WatchAdForPoints.jsx`

## Main components

- `src-frontend/components/PangleSplashAd.jsx`
- `src-frontend/components/PangleCarouselAd.jsx`
- `src-frontend/components/PangleBannerAd.jsx`
- `src-frontend/config/pangle.js`

## Local verification

1. Copy `env.example` to `.env`
2. Fill in real Pangle slot IDs
3. Start the frontend with `npm run dev`
4. Use a mobile device or browser mobile emulation
5. Open the homepage and the credits entry to confirm ads render

## Docker deployment

For Docker builds, set these values in `.env` or `docker-compose.env.example`:

```env
VITE_PANGLE_SLOT_ID=
VITE_PANGLE_CAROUSEL_SLOT_ID=
VITE_PANGLE_SPLASH_SLOT_ID=
```

Then rebuild the frontend image:

```powershell
docker compose build frontend
docker compose up -d frontend
```
