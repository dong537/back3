// This is a "killer" service worker. Its only purpose is to unregister itself
// and any other service workers to fix caching issues.

self.addEventListener('install', () => {
  self.skipWaiting();
});

self.addEventListener('activate', event => {
  event.waitUntil(
    (async () => {
      // Unregister all service workers.
      const registrations = await self.registration.unregister();
      console.log('All service workers unregistered:', registrations);

      // Delete all caches.
      const keys = await caches.keys();
      await Promise.all(keys.map(key => caches.delete(key)));
      console.log('All caches deleted.');

      // Force-reload all clients.
      const clients = await self.clients.matchAll({ type: 'window' });
      clients.forEach(client => client.navigate(client.url));
    })()
  );
});
