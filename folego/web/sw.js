// Cache do app para funcionar offline (versão web/PWA).
// Troque o número da versão a cada atualização publicada.
const CACHE = 'folego-v2';
const ASSETS = [
  './', './index.html', './privacy.html', './manifest.webmanifest',
  './icon-192.png', './icon-512.png',
  './fonts/inter-latin.woff2', './fonts/space-grotesk-latin.woff2'
];

self.addEventListener('install', e => {
  e.waitUntil(caches.open(CACHE).then(c => c.addAll(ASSETS)).then(() => self.skipWaiting()));
});

self.addEventListener('activate', e => {
  e.waitUntil(
    caches.keys()
      .then(keys => Promise.all(keys.filter(k => k !== CACHE).map(k => caches.delete(k))))
      .then(() => self.clients.claim())
  );
});

// Responde na hora com o que está em cache e atualiza em segundo plano,
// assim uma versão nova chega na próxima abertura sem precisar limpar nada.
self.addEventListener('fetch', e => {
  if (e.request.method !== 'GET' || new URL(e.request.url).origin !== location.origin) return;
  e.respondWith(
    caches.open(CACHE).then(cache =>
      cache.match(e.request).then(hit => {
        const net = fetch(e.request).then(res => {
          if (res.ok) cache.put(e.request, res.clone());
          return res;
        }).catch(() => hit || cache.match('./index.html'));
        return hit || net;
      })
    )
  );
});
