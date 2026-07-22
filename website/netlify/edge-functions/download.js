const FILES = {
  windows: {
    url: 'https://github.com/Tharun-code-08/LibraryManagementSystem/releases/latest/download/LibraryManagementSystem-Setup.exe',
    filename: 'LibraryManagementSystem-Setup.exe',
  },
  macos: {
    url: 'https://github.com/Tharun-code-08/LibraryManagementSystem/releases/latest/download/LibraryManagementSystem.dmg',
    filename: 'LibraryManagementSystem.dmg',
  },
  linux: {
    url: 'https://github.com/Tharun-code-08/LibraryManagementSystem/releases/latest/download/LibraryManagementSystem.deb',
    filename: 'LibraryManagementSystem.deb',
  },
};

export default async (request) => {
  const platform = new URL(request.url).pathname.split('/').pop();
  const target = FILES[platform];

  if (!target) {
    return new Response('Not found', { status: 404 });
  }

  // Follow GitHub's redirect chain server-side — client never sees GitHub
  const upstream = await fetch(target.url, { redirect: 'follow' });

  if (!upstream.ok) {
    return new Response('File temporarily unavailable', { status: 502 });
  }

  // Stream the body — never buffered in memory, works for 100MB+ files
  return new Response(upstream.body, {
    status: 200,
    headers: {
      'Content-Type': 'application/octet-stream',
      'Content-Disposition': `attachment; filename="${target.filename}"`,
      'Content-Length': upstream.headers.get('Content-Length') ?? '',
      'Cache-Control': 'no-cache',
    },
  });
};

export const config = { path: '/download/:platform' };
