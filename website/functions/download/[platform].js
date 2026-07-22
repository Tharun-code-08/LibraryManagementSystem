const FILES = {
  windows: {
    url: 'https://github.com/Tharun-code-08/LibraryManagementSystem/releases/latest/download/LibraryManagementSystem-Windows.zip',
    filename: 'LibraryManagementSystem-Windows.zip',
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

export async function onRequest(context) {
  const platform = context.params.platform;
  const target = FILES[platform];

  if (!target) {
    return new Response('Not found', { status: 404 });
  }

  const upstream = await fetch(target.url, { redirect: 'follow' });

  if (!upstream.ok) {
    return new Response('File temporarily unavailable', { status: 502 });
  }

  return new Response(upstream.body, {
    status: 200,
    headers: {
      'Content-Type': 'application/octet-stream',
      'Content-Disposition': `attachment; filename="${target.filename}"`,
      'Content-Length': upstream.headers.get('Content-Length') ?? '',
      'Cache-Control': 'no-cache',
    },
  });
}
