const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 3000;
const ROOT = '/app/applet';

const MIME_TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.json': 'application/json',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon',
  '.apk': 'application/vnd.android.package-archive',
  '.zip': 'application/zip',
  '.woff2': 'font/woff2',
  '.ttf': 'font/ttf',
  '.webmanifest': 'application/manifest+json'
};

process.on('uncaughtException', (err) => {
  if (err.code === 'EPIPE' || err.code === 'ECONNRESET') return;
  console.error('Uncaught exception:', err);
});

const server = http.createServer((req, res) => {
  req.on('error', () => {});
  res.on('error', () => {});

  const parsedUrl = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
  let pathname = decodeURIComponent(parsedUrl.pathname);

  if (pathname === '/') {
    pathname = '/index.html';
  }

  const safePath = path.normalize(pathname).replace(/^(\.\.[\/\\])+/, '');
  let filePath = path.join(ROOT, safePath);

  // If asking for APK, also check build output fallback
  if (pathname.endsWith('.apk') && !fs.existsSync(filePath)) {
    const fallbackApk = path.join(ROOT, 'app/build/outputs/apk/debug/app-debug.apk');
    if (fs.existsSync(fallbackApk)) {
      filePath = fallbackApk;
    }
  }

  fs.stat(filePath, (err, stats) => {
    if (err || !stats.isFile()) {
      res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
      res.end('File Not Found');
      return;
    }

    const ext = path.extname(filePath).toLowerCase();
    const contentType = MIME_TYPES[ext] || 'application/octet-stream';
    const totalSize = stats.size;

    const headers = {
      'Content-Type': contentType,
      'Accept-Ranges': 'bytes',
      'Last-Modified': stats.mtime.toUTCString(),
      'Cache-Control': ext === '.apk' || ext === '.zip' ? 'public, max-age=3600' : 'no-cache'
    };

    if (ext === '.apk') {
      headers['Content-Disposition'] = 'attachment; filename="ariana-sandoogh.apk"';
    } else if (ext === '.zip') {
      headers['Content-Disposition'] = 'attachment; filename="sandoogh-pwa-updated.zip"';
    }

    // Range request support for download managers and mobile browsers
    const range = req.headers.range;
    if (range) {
      const parts = range.replace(/bytes=/, '').split('-');
      const start = parseInt(parts[0], 10);
      const end = parts[1] ? parseInt(parts[1], 10) : totalSize - 1;

      if (start >= totalSize || end >= totalSize || start > end) {
        res.writeHead(416, { 'Content-Range': `bytes */${totalSize}` });
        res.end();
        return;
      }

      headers['Content-Range'] = `bytes ${start}-${end}/${totalSize}`;
      headers['Content-Length'] = (end - start + 1);
      res.writeHead(206, headers);

      if (req.method === 'HEAD') {
        res.end();
        return;
      }

      const stream = fs.createReadStream(filePath, { start, end });
      stream.on('error', () => { stream.destroy(); });
      res.on('close', () => { stream.destroy(); });
      stream.pipe(res);
    } else {
      headers['Content-Length'] = totalSize;
      res.writeHead(200, headers);

      if (req.method === 'HEAD') {
        res.end();
        return;
      }

      const stream = fs.createReadStream(filePath);
      stream.on('error', () => { stream.destroy(); });
      res.on('close', () => { stream.destroy(); });
      stream.pipe(res);
    }
  });
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`Static server running on http://0.0.0.0:${PORT} serving ${ROOT}`);
});
