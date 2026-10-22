(function () {
  'use strict';

  const ALPHABET = '123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz';
  const TOKEN_PROGRAM_ID = 'TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA';
  const TOKEN_2022_PROGRAM_ID = 'TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb';
  const ASSOCIATED_TOKEN_PROGRAM_ID = 'ATokenGPvbdGVxr1b2hvZbsiqW5xWH25efTNsLJA8knL';

  function base58Encode(bytes) {
    let n = 0n;
    for (const byte of bytes) n = n * 256n + BigInt(byte);
    let text = '';
    while (n > 0n) {
      text = ALPHABET[Number(n % 58n)] + text;
      n /= 58n;
    }
    for (const byte of bytes) {
      if (byte !== 0) break;
      text = '1' + text;
    }
    return text;
  }

  function base58Decode(text) {
    let n = 0n;
    for (const char of text) {
      const digit = ALPHABET.indexOf(char);
      if (digit < 0) throw new Error('Invalid base58 character: ' + char);
      n = n * 58n + BigInt(digit);
    }
    const bytes = [];
    while (n > 0n) {
      bytes.unshift(Number(n % 256n));
      n /= 256n;
    }
    for (const char of text) {
      if (char !== '1') break;
      bytes.unshift(0);
    }
    return Uint8Array.from(bytes);
  }

  function decodeAddress(text) {
    // 32 bytes never need more than 44 chars; also stops BigInt decoding of huge pastes freezing the page.
    if (text.length > 44) return null;
    try {
      const bytes = base58Decode(text);
      return bytes.length === 32 ? bytes : null;
    } catch {
      return null;
    }
  }

  const P = 2n ** 255n - 19n;

  function mod(a) {
    const r = a % P;
    return r < 0n ? r + P : r;
  }

  function modPow(base, exponent) {
    let result = 1n;
    base = mod(base);
    while (exponent > 0n) {
      if (exponent & 1n) result = (result * base) % P;
      base = (base * base) % P;
      exponent >>= 1n;
    }
    return result;
  }

  const D = mod(-121665n * modPow(121666n, P - 2n));

  // Euler's criterion: on curve iff x² = (y² - 1) / (d·y² + 1) is a square mod p.
  function isOnCurve(bytes) {
    let y = 0n;
    for (let i = 31; i >= 0; i--) y = (y << 8n) | BigInt(bytes[i]);
    // The top bit holds the sign of x, not part of y.
    y &= (1n << 255n) - 1n;
    const y2 = mod(y * y);
    const x2 = mod((y2 - 1n) * modPow(D * y2 + 1n, P - 2n));
    return modPow(x2, (P - 1n) / 2n) <= 1n;
  }

  const PDA_MARKER = new TextEncoder().encode('ProgramDerivedAddress');

  // Same search as PublicKey.findProgramAddress in sol4k: the first bump from
  // 255 down whose hash is off the curve, so no private key can exist for it.
  async function findProgramAddress(seeds, programId) {
    for (let bump = 255; bump >= 1; bump--) {
      const input = Uint8Array.from(
        [...seeds, [bump], programId, PDA_MARKER].flatMap((part) => [...part]),
      );
      const hash = new Uint8Array(await crypto.subtle.digest('SHA-256', input));
      if (!isOnCurve(hash)) return { address: hash, bump, tries: 256 - bump };
    }
    throw new Error('Unable to find program address');
  }

  function findAssociatedTokenAddress(holder, mint, tokenProgramId) {
    return findProgramAddress(
      [holder, base58Decode(tokenProgramId), mint],
      base58Decode(ASSOCIATED_TOKEN_PROGRAM_ID),
    );
  }

  async function randomWalletAddress() {
    try {
      // Private key stays non-extractable and is dropped; only the public half is exported.
      const pair = await crypto.subtle.generateKey({ name: 'Ed25519' }, false, ['sign', 'verify']);
      return base58Encode(new Uint8Array(await crypto.subtle.exportKey('raw', pair.publicKey)));
    } catch {
      // Browsers without WebCrypto Ed25519: a random on-curve point is a valid wallet-style address.
      const bytes = new Uint8Array(32);
      do crypto.getRandomValues(bytes); while (!isOnCurve(bytes));
      return base58Encode(bytes);
    }
  }

  const short = (address) => address.slice(0, 4) + '…' + address.slice(-4);

  function span(className, text) {
    const el = document.createElement('span');
    el.className = className;
    el.textContent = text;
    return el;
  }

  function renderKotlin(code, holder, mint, is2022, result) {
    const lines = [
      [['kd', 'val'], ['', ' '], ['py', 'programDerivedAddress'], ['', ' '], ['p', '='], ['', ' '],
        ['nc', 'PublicKey'], ['p', '.'], ['nf', 'findProgramDerivedAddress'], ['p', '(']],
      [['', '    '], ['nc', 'PublicKey'], ['p', '('], ['s', '"' + holder + '"'], ['p', '),']],
      [['', '    '], ['nc', 'PublicKey'], ['p', '('], ['s', '"' + mint + '"'], ['p', '),']],
    ];
    if (is2022) lines.push([['', '    '], ['nc', 'Constants'], ['p', '.'], ['py', 'TOKEN_2022_PROGRAM_ID'], ['p', ',']]);
    lines.push([['p', ')']]);
    if (result) {
      lines.push([['n', 'programDerivedAddress'], ['p', '.'], ['py', 'publicKey'], ['', ' '], ['c1', '// ' + short(result.address)]]);
      lines.push([['n', 'programDerivedAddress'], ['p', '.'], ['py', 'nonce'], ['', ' '], ['c1', '// ' + result.bump]]);
    }
    code.replaceChildren(...lines.flatMap((line) => [...line.map(([c, t]) => span(c, t)), '\n']));
  }

  function mount(root) {
    const form = root.querySelector('form');
    const holderInput = form.elements.holder;
    const mintInput = form.elements.mint;
    const out = (name) => root.querySelector('[data-out="' + name + '"]');
    let generation = 0;

    async function update() {
      const run = ++generation;
      const is2022 = form.elements.program.value === '2022';
      const tokenProgramId = is2022 ? TOKEN_2022_PROGRAM_ID : TOKEN_PROGRAM_ID;
      const holderText = holderInput.value.trim();
      const mintText = mintInput.value.trim();
      const holder = decodeAddress(holderText);
      const mint = decodeAddress(mintText);
      holderInput.setAttribute('aria-invalid', String(!holder));
      mintInput.setAttribute('aria-invalid', String(!mint));

      out('seed-holder').textContent = holder ? short(holderText) : '?';
      out('seed-program').textContent = short(tokenProgramId);
      out('seed-program').title = tokenProgramId;
      out('seed-program-name').textContent = is2022 ? 'Token-2022 program' : 'Token program';
      out('seed-mint').textContent = mint ? short(mintText) : '?';
      out('program-note').hidden = !is2022;

      let result = null;
      if (holder && mint) {
        const found = await findAssociatedTokenAddress(holder, mint, tokenProgramId);
        if (run !== generation) return;
        result = { address: base58Encode(found.address), bump: found.bump, tries: found.tries };
      }
      out('address').textContent = result ? result.address : 'Enter two valid base58 addresses';
      out('address').classList.toggle('ata-muted', !result);
      out('bump').textContent = result
        ? result.bump + (result.tries > 1 ? ' (' + (result.tries - 1) + ' on-curve hash skipped)' : '')
        : '–';
      renderKotlin(out('kotlin'), holderText || '…', mintText || '…', is2022, result);
    }

    form.addEventListener('input', update);
    form.addEventListener('submit', (event) => event.preventDefault());
    root.querySelector('[data-action="random"]').addEventListener('click', async () => {
      holderInput.value = await randomWalletAddress();
      update();
    });
    update();
  }

  const template = document.getElementById('ata-widget-template');
  const anchor = [...document.querySelectorAll('.highlighter-rouge')]
    .filter((block) => block.textContent.includes('findProgramDerivedAddress(holderAddress'))
    .pop();
  // WebCrypto digest only exists in secure contexts (https, localhost); without it the page stays as is.
  if (!template || !anchor || !(window.crypto && crypto.subtle)) return;
  const root = template.content.firstElementChild.cloneNode(true);
  anchor.after(root);
  mount(root);
})();
