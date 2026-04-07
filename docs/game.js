// Coral Drift — HTML5 Canvas edition
// A cute endless runner with a pink octopus

(() => {
  // ─── Setup ───────────────────────────────────────────────────────────────
  const LOGICAL_W = 1280;
  const LOGICAL_H = 720;
  const canvas = document.getElementById('game');
  const ctx = canvas.getContext('2d');
  const loading = document.getElementById('loading');

  let scale = 1;
  let offsetX = 0;
  let offsetY = 0;

  function resize() {
    const dpr = Math.min(window.devicePixelRatio || 1, 2);
    const w = window.innerWidth;
    const h = window.innerHeight;
    scale = Math.min(w / LOGICAL_W, h / LOGICAL_H);
    const displayW = LOGICAL_W * scale;
    const displayH = LOGICAL_H * scale;
    offsetX = (w - displayW) / 2;
    offsetY = (h - displayH) / 2;
    canvas.style.width = w + 'px';
    canvas.style.height = h + 'px';
    canvas.width = Math.floor(w * dpr);
    canvas.height = Math.floor(h * dpr);
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
  }
  window.addEventListener('resize', resize);
  resize();

  // ─── Constants ───────────────────────────────────────────────────────────
  const GROUND_Y = 580;
  const PLAYER_X = 220;
  const PLAYER_W = 80;
  const PLAYER_H = 70;
  const GRAVITY = 1650;
  const JUMP_V = -820;
  const BASE_SPEED = 320;
  // Physics note: max jump height = 820²/(2·1650) ≈ 203px
  // → max clearable coral top = 203 + 10 (hitbox slack) = 213px above ground
  // Keep all coral heights well below this so jumps are always achievable.

  const PINK = '#ff69b4';
  const PINK_LIGHT = '#ff85c1';
  const PINK_SOFT = '#ffb6d9';
  const PINK_DARK = '#c84791';
  const HEART_PINK = '#ff6b7a';
  const TEAL = '#4ecdc4';
  const GOLD = '#ffd93d';

  // ─── Audio (Web Audio synthesis) ─────────────────────────────────────────
  let audioCtx = null;
  let soundOn = true;
  function initAudio() {
    if (!audioCtx) {
      try { audioCtx = new (window.AudioContext || window.webkitAudioContext)(); }
      catch (e) { audioCtx = null; }
    }
    if (audioCtx && audioCtx.state === 'suspended') audioCtx.resume();
  }
  function playTone(freqs, dur, type = 'sine', volume = 0.15) {
    if (!soundOn || !audioCtx) return;
    const now = audioCtx.currentTime;
    const osc = audioCtx.createOscillator();
    const gain = audioCtx.createGain();
    osc.type = type;
    osc.frequency.setValueAtTime(freqs[0], now);
    for (let i = 1; i < freqs.length; i++) {
      osc.frequency.linearRampToValueAtTime(freqs[i], now + (dur * i / (freqs.length - 1)));
    }
    gain.gain.setValueAtTime(0, now);
    gain.gain.linearRampToValueAtTime(volume, now + 0.005);
    gain.gain.linearRampToValueAtTime(0, now + dur);
    osc.connect(gain); gain.connect(audioCtx.destination);
    osc.start(now);
    osc.stop(now + dur + 0.05);
  }
  const sfx = {
    jump:    () => playTone([320, 520], 0.12, 'sine', 0.12),
    collect: () => playTone([660, 880, 1100], 0.18, 'sine', 0.14),
    hit:     () => playTone([200, 80], 0.3, 'sawtooth', 0.18),
    gameover:() => playTone([300, 240, 180, 150], 0.8, 'triangle', 0.15),
    chain:   (lvl) => playTone([440 * (1 + lvl * 0.12), 550 * (1 + lvl * 0.12)], 0.15, 'sine', 0.12),
  };

  // ─── Math helpers ────────────────────────────────────────────────────────
  const rand = (a, b) => a + Math.random() * (b - a);
  const chance = (p) => Math.random() < p;
  const lerp = (a, b, t) => a + (b - a) * t;
  const clamp = (v, mn, mx) => Math.max(mn, Math.min(mx, v));

  // ─── Game state ──────────────────────────────────────────────────────────
  const STATE_MENU = 0, STATE_PLAYING = 1, STATE_GAMEOVER = 2;
  let state = STATE_MENU;
  let score = 0, hearts = 0, chain = 0, chainTimer = 0, maxChain = 0;
  let bestScore = parseInt(localStorage.getItem('coralDriftBest') || '0');
  let speedScale = 1;
  let worldTime = 0;
  let cameraShake = 0;

  // ─── Player (octopus) ────────────────────────────────────────────────────
  const player = {
    x: PLAYER_X, y: GROUND_Y - PLAYER_H, vy: 0,
    onGround: true, animTime: 0,
    squash: 1, squashTarget: 1,
    blinkTimer: 0, nextBlink: 2.5, blinking: false, blinkProgress: 0,
    expression: 'normal', // normal, happy, dead
    expressionTimer: 0,
    hasShield: false,
    shieldPulse: 0,
    reset() {
      this.y = GROUND_Y - PLAYER_H; this.vy = 0; this.onGround = true;
      this.squash = 1; this.expression = 'normal'; this.expressionTimer = 0;
      this.hasShield = false; this.shieldPulse = 0;
    },
    jump() {
      if (this.onGround) {
        this.vy = JUMP_V; this.onGround = false; this.squash = 1.25;
        sfx.jump();
        spawnBlup(this.x + this.width / 2 + 25, this.y + 10);
      }
    },
    get width() { return PLAYER_W; },
    get height() { return PLAYER_H; },
    update(dt) {
      this.animTime += dt;
      if (!this.onGround) {
        this.vy += GRAVITY * dt;
        this.y += this.vy * dt;
        if (this.y + PLAYER_H >= GROUND_Y) {
          this.y = GROUND_Y - PLAYER_H; this.vy = 0; this.onGround = true;
          this.squash = 0.85;
        }
      }
      // Squash recovery
      this.squash = lerp(this.squash, this.squashTarget, dt * 12);
      if (!this.onGround && this.vy < 0) this.squashTarget = 1.15;
      else if (!this.onGround) this.squashTarget = 1.08;
      else this.squashTarget = 1.0;

      // Blink
      if (this.expression !== 'dead') {
        this.blinkTimer += dt;
        if (!this.blinking && this.blinkTimer >= this.nextBlink) {
          this.blinking = true; this.blinkProgress = 0; this.blinkTimer = 0;
          this.nextBlink = rand(2, 4.5);
        }
        if (this.blinking) {
          this.blinkProgress += dt / 0.075;
          if (this.blinkProgress >= 2) { this.blinking = false; this.blinkProgress = 0; }
        }
      }

      // Expression timer
      if (this.expressionTimer > 0) {
        this.expressionTimer -= dt;
        if (this.expressionTimer <= 0 && this.expression !== 'dead') this.expression = 'normal';
      }

      // Shield pulse (visual only)
      if (this.hasShield) this.shieldPulse += dt * 4;
    },
    setHappy(dur) { this.expression = 'happy'; this.expressionTimer = dur; },
  };

  // ─── Octopus renderer ────────────────────────────────────────────────────
  function drawOctopus(x, y, w, h) {
    ctx.save();
    const cx = x + w / 2;
    const cy = y + h / 2;
    const bob = Math.sin(player.animTime * 4) * 4;
    const sy = player.squash;
    const sx = 1 / player.squash;
    ctx.translate(cx, cy + bob);
    ctx.scale(sx, sy);
    ctx.translate(-cx, -cy);

    // Tentacles (6, fanned)
    drawTentacles(x, y, w, h);

    // Body
    drawBody(x, y, w, h);

    // Face
    drawFace(x, y, w, h);

    // Shimmer
    drawShimmer(x, y, w, h);

    ctx.restore();

    // Ambient sparkles
    drawSparkles(cx, cy + bob, Math.max(w, h));

    // Shield bubble around the octopus when active
    if (player.hasShield) {
      const r = Math.max(w, h) * 0.95;
      const pulse = 1 + Math.sin(player.shieldPulse) * 0.05;
      const rr = r * pulse;
      // Outer glow
      const og = ctx.createRadialGradient(cx, cy + bob, rr * 0.6, cx, cy + bob, rr);
      og.addColorStop(0, 'rgba(130, 210, 255, 0)');
      og.addColorStop(0.7, 'rgba(130, 210, 255, 0.12)');
      og.addColorStop(1, 'rgba(130, 210, 255, 0.35)');
      ctx.fillStyle = og;
      ctx.beginPath(); ctx.arc(cx, cy + bob, rr, 0, Math.PI * 2); ctx.fill();
      // Main bubble
      const g = ctx.createRadialGradient(cx - rr * 0.3, cy + bob - rr * 0.3, rr * 0.2, cx, cy + bob, rr);
      g.addColorStop(0, 'rgba(220, 240, 255, 0.15)');
      g.addColorStop(0.7, 'rgba(130, 200, 255, 0.15)');
      g.addColorStop(1, 'rgba(90, 180, 220, 0.30)');
      ctx.fillStyle = g;
      ctx.beginPath(); ctx.arc(cx, cy + bob, rr, 0, Math.PI * 2); ctx.fill();
      // Edge
      ctx.strokeStyle = 'rgba(180, 230, 255, 0.7)';
      ctx.lineWidth = 2.5;
      ctx.beginPath(); ctx.arc(cx, cy + bob, rr, 0, Math.PI * 2); ctx.stroke();
      // Highlight
      ctx.fillStyle = 'rgba(255, 255, 255, 0.5)';
      ctx.beginPath();
      ctx.ellipse(cx - rr * 0.35, cy + bob - rr * 0.45, rr * 0.2, rr * 0.1, -0.5, 0, Math.PI * 2);
      ctx.fill();
    }
  }

  function drawTentacles(x, y, w, h) {
    const cx0 = x + w / 2;
    const bodyBottom = y + h * 0.58;
    const n = 6;
    ctx.lineCap = 'round';
    for (let i = 0; i < n; i++) {
      const t = i / (n - 1);
      const spreadDeg = -60 + t * 120;
      const spreadRad = spreadDeg * Math.PI / 180;
      const phase = player.animTime * 2.3 + i * 1.05;
      const wave = Math.sin(phase) * 7;
      const baseX = cx0 + Math.sin(spreadRad) * w * 0.28;
      const lenFactor = 0.52 + 0.11 * (1 - Math.abs(t - 0.5) * 2);
      const len = h * lenFactor;
      const endX = baseX + Math.sin(spreadRad) * len * 0.5 + wave;
      const endY = bodyBottom + len;
      const cp1x = baseX + Math.sin(spreadRad) * len * 0.18 + wave * 0.3;
      const cp1y = bodyBottom + len * 0.32;
      const cp2x = baseX + Math.sin(spreadRad) * len * 0.38 + wave;
      const cp2y = bodyBottom + len * 0.72;
      const baseW = w * 0.068;
      // 3-pass tapered stroke
      ctx.strokeStyle = PINK_DARK; ctx.lineWidth = baseW;
      ctx.beginPath(); ctx.moveTo(baseX, bodyBottom); ctx.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, endX, endY); ctx.stroke();
      ctx.strokeStyle = PINK; ctx.lineWidth = baseW * 0.7;
      ctx.beginPath(); ctx.moveTo(baseX, bodyBottom); ctx.bezierCurveTo(cp1x + 1, cp1y, cp2x + 1, cp2y, endX, endY); ctx.stroke();
      ctx.strokeStyle = PINK_SOFT; ctx.lineWidth = baseW * 0.28;
      ctx.beginPath(); ctx.moveTo(baseX, bodyBottom); ctx.bezierCurveTo(cp1x + 1, cp1y - 2, cp2x + 1, cp2y - 2, endX, endY); ctx.stroke();
      // Suckers
      ctx.fillStyle = PINK_SOFT;
      for (const tt of [0.3, 0.55, 0.75]) {
        const u = 1 - tt;
        const px = u*u*u*baseX + 3*u*u*tt*cp1x + 3*u*tt*tt*cp2x + tt*tt*tt*endX;
        const py = u*u*u*bodyBottom + 3*u*u*tt*cp1y + 3*u*tt*tt*cp2y + tt*tt*tt*endY;
        const sd = 2.2 * (1 - tt * 0.5);
        ctx.beginPath(); ctx.arc(px, py, sd, 0, Math.PI * 2); ctx.fill();
      }
    }
  }

  function drawBody(x, y, w, h) {
    const bodyW = w * 0.85;
    const bodyH = h * 0.65;
    const bodyX = x + (w - bodyW) / 2;
    // Shadow
    ctx.fillStyle = 'rgba(80, 40, 80, 0.2)';
    ctx.beginPath(); ctx.ellipse(bodyX + bodyW/2 + 4, y + bodyH/2 + 5, bodyW/2, bodyH/2, 0, 0, Math.PI * 2); ctx.fill();
    // Main body gradient
    const grad = ctx.createRadialGradient(bodyX + bodyW * 0.3, y + bodyH * 0.25, bodyW * 0.1, bodyX + bodyW * 0.5, y + bodyH * 0.5, bodyW * 0.8);
    grad.addColorStop(0, '#ffc8e6');
    grad.addColorStop(0.3, PINK_LIGHT);
    grad.addColorStop(0.7, PINK);
    grad.addColorStop(1, '#c85090');
    ctx.fillStyle = grad;
    ctx.beginPath(); ctx.ellipse(bodyX + bodyW/2, y + bodyH/2, bodyW/2, bodyH/2, 0, 0, Math.PI * 2); ctx.fill();
    // Outline
    ctx.strokeStyle = PINK_DARK; ctx.lineWidth = 2;
    ctx.beginPath(); ctx.ellipse(bodyX + bodyW/2, y + bodyH/2, bodyW/2, bodyH/2, 0, 0, Math.PI * 2); ctx.stroke();
    // Highlight
    ctx.fillStyle = 'rgba(255, 255, 255, 0.3)';
    ctx.beginPath(); ctx.ellipse(bodyX + bodyW * 0.3, y + bodyH * 0.22, bodyW * 0.15, bodyH * 0.12, 0, 0, Math.PI * 2); ctx.fill();
  }

  function drawFace(x, y, w, h) {
    const bodyW = w * 0.85;
    const bodyH = h * 0.65;
    const bodyX = x + (w - bodyW) / 2;
    const bcx = bodyX + bodyW / 2;
    const bcy = y + bodyH * 0.45;
    const eyeSpacing = bodyW * 0.25;
    const eyeSize = bodyW * 0.22;
    const pupilSize = eyeSize * 0.5;

    // Eyebrow arches (cute)
    if (player.expression !== 'dead') {
      ctx.strokeStyle = PINK_DARK; ctx.lineWidth = 2;
      const browY = bcy - eyeSize * 0.8;
      ctx.beginPath(); ctx.arc(bcx - eyeSpacing, browY, eyeSize * 0.4, Math.PI, 0); ctx.stroke();
      ctx.beginPath(); ctx.arc(bcx + eyeSpacing, browY, eyeSize * 0.4, Math.PI, 0); ctx.stroke();
    }

    // Eyes
    drawEye(bcx - eyeSpacing, bcy, eyeSize, pupilSize);
    drawEye(bcx + eyeSpacing, bcy, eyeSize, pupilSize);

    // Blush
    ctx.fillStyle = '#ff9ecd';
    ctx.beginPath(); ctx.ellipse(bcx - eyeSpacing * 1.6, bcy + eyeSize * 0.5, bodyW * 0.06, bodyW * 0.035, 0, 0, Math.PI * 2); ctx.fill();
    ctx.beginPath(); ctx.ellipse(bcx + eyeSpacing * 1.6, bcy + eyeSize * 0.5, bodyW * 0.06, bodyW * 0.035, 0, 0, Math.PI * 2); ctx.fill();

    // Mouth
    ctx.strokeStyle = '#3a1a30'; ctx.lineWidth = 2.5;
    const mY = bcy + eyeSize * 0.85;
    ctx.beginPath();
    if (player.expression === 'dead') {
      ctx.arc(bcx, mY + 4, 8, Math.PI * 0.2, Math.PI * 0.8);
    } else if (player.expression === 'happy') {
      ctx.arc(bcx, mY - 2, 11, 0.15 * Math.PI, 0.85 * Math.PI);
    } else {
      ctx.arc(bcx, mY - 2, 8, 0.15 * Math.PI, 0.85 * Math.PI);
    }
    ctx.stroke();
  }

  function drawEye(cx, cy, size, pupilSize) {
    // Eye white
    const grad = ctx.createRadialGradient(cx - size*0.15, cy - size*0.15, size*0.1, cx, cy, size*0.6);
    grad.addColorStop(0, '#ffffff');
    grad.addColorStop(1, '#dce6f5');
    ctx.fillStyle = grad;
    ctx.beginPath(); ctx.arc(cx, cy, size/2, 0, Math.PI * 2); ctx.fill();
    ctx.strokeStyle = 'rgba(80,80,80,0.4)'; ctx.lineWidth = 1;
    ctx.beginPath(); ctx.arc(cx, cy, size/2, 0, Math.PI * 2); ctx.stroke();

    // Pupil
    const lookOffset = Math.sin(player.animTime * 0.5) * 2;
    const px = cx + lookOffset;
    const py = cy;

    if (player.expression === 'dead') {
      ctx.strokeStyle = '#281432'; ctx.lineWidth = 2.5;
      const xs = size * 0.32;
      ctx.beginPath(); ctx.moveTo(px - xs, py - xs); ctx.lineTo(px + xs, py + xs); ctx.stroke();
      ctx.beginPath(); ctx.moveTo(px + xs, py - xs); ctx.lineTo(px - xs, py + xs); ctx.stroke();
    } else if (player.expression === 'happy') {
      // Star pupil
      ctx.fillStyle = GOLD;
      drawStar(px, py, pupilSize * 0.55, 5);
      ctx.fillStyle = 'rgba(255,255,255,0.9)';
      ctx.beginPath(); ctx.arc(px - pupilSize * 0.2, py - pupilSize * 0.25, pupilSize * 0.15, 0, Math.PI * 2); ctx.fill();
    } else {
      // Heart pupil
      ctx.fillStyle = '#281432';
      drawHeart(px, py, pupilSize * 0.55);
      ctx.fillStyle = 'rgba(255,255,255,0.9)';
      ctx.beginPath(); ctx.arc(px - pupilSize * 0.2, py - pupilSize * 0.25, pupilSize * 0.14, 0, Math.PI * 2); ctx.fill();
    }

    // Blink eyelid
    if (player.blinking && player.blinkProgress > 0) {
      const lidFraction = player.blinkProgress <= 1 ? player.blinkProgress : 2 - player.blinkProgress;
      ctx.fillStyle = PINK;
      ctx.fillRect(cx - size/2 - 1, cy - size/2 - 1, size + 2, size * lidFraction + 1);
    }
  }

  function drawHeart(cx, cy, size) {
    ctx.beginPath();
    ctx.moveTo(cx, cy + size * 0.4);
    ctx.bezierCurveTo(cx - size * 1.2, cy - size * 0.3, cx - size * 0.4, cy - size * 1.0, cx, cy - size * 0.3);
    ctx.bezierCurveTo(cx + size * 0.4, cy - size * 1.0, cx + size * 1.2, cy - size * 0.3, cx, cy + size * 0.4);
    ctx.closePath();
    ctx.fill();
  }

  function drawStar(cx, cy, size, points) {
    ctx.beginPath();
    for (let i = 0; i < points * 2; i++) {
      const angle = Math.PI * i / points - Math.PI / 2;
      const r = (i % 2 === 0) ? size : size * 0.42;
      const sx = cx + Math.cos(angle) * r;
      const sy = cy + Math.sin(angle) * r;
      if (i === 0) ctx.moveTo(sx, sy); else ctx.lineTo(sx, sy);
    }
    ctx.closePath();
    ctx.fill();
  }

  function drawShimmer(x, y, w, h) {
    const bodyW = w * 0.85;
    const bodyX = x + (w - bodyW) / 2;
    const shimmerPhase = (player.animTime * 0.8) % 1;
    if (shimmerPhase > 0.1 && shimmerPhase < 0.9) {
      const shimmerAlpha = 0.45 * (1 - Math.abs(shimmerPhase - 0.5) * 2);
      ctx.fillStyle = `rgba(255,255,255,${shimmerAlpha})`;
      ctx.beginPath();
      ctx.ellipse(bodyX + shimmerPhase * bodyW, y + h * 0.2, 10, 4.5, 0, 0, Math.PI * 2);
      ctx.fill();
    }
  }

  function drawSparkles(cx, cy, radius) {
    for (let i = 0; i < 6; i++) {
      const angle = player.animTime * 0.7 + i * Math.PI / 3;
      const dist = radius * (0.5 + 0.4 * Math.sin(player.animTime * 1.8 + i));
      const sx = cx + Math.cos(angle) * dist;
      const sy = cy + Math.sin(angle) * dist;
      const alpha = 0.4 + 0.5 * Math.abs(Math.sin(player.animTime * 3 + i));
      const size = 2.5 + 1.5 * Math.abs(Math.sin(player.animTime * 2 + i));
      ctx.fillStyle = `hsla(${(player.animTime * 60 + i * 60) % 360}, 70%, 80%, ${alpha})`;
      ctx.beginPath(); ctx.arc(sx, sy, size, 0, Math.PI * 2); ctx.fill();
    }
  }

  // ─── Background ──────────────────────────────────────────────────────────
  const bgLayers = [
    { speed: 0.2, color: '#0d2440', elements: [] },
    { speed: 0.4, color: '#0f3050', elements: [] },
    { speed: 0.7, color: '#134060', elements: [] },
  ];
  const bubbles = [];
  const stars = [];

  function initBackground() {
    // Seaweed-like silhouettes
    bgLayers.forEach((layer, idx) => {
      for (let i = 0; i < 8; i++) {
        layer.elements.push({
          x: rand(0, LOGICAL_W * 2),
          y: GROUND_Y - rand(50, 180),
          w: rand(30, 80),
          h: rand(80, 180),
          phase: rand(0, Math.PI * 2),
        });
      }
    });
    // Bubbles
    for (let i = 0; i < 30; i++) {
      bubbles.push({
        x: rand(0, LOGICAL_W),
        y: rand(0, LOGICAL_H),
        r: rand(4, 12),
        vy: rand(-40, -15),
        phase: rand(0, Math.PI * 2),
      });
    }
    // Falling star motes
    for (let i = 0; i < 40; i++) {
      stars.push({
        x: rand(0, LOGICAL_W),
        y: rand(0, LOGICAL_H),
        r: rand(1, 2.5),
        vy: rand(8, 25),
        twinkle: rand(0, Math.PI * 2),
        hue: rand(180, 240),
      });
    }
  }

  function updateBackground(dt, speed) {
    bgLayers.forEach(layer => {
      layer.elements.forEach(e => {
        e.x -= speed * layer.speed * dt;
        if (e.x + e.w < 0) { e.x = LOGICAL_W + rand(0, 300); e.y = GROUND_Y - rand(50, 180); }
      });
    });
    bubbles.forEach(b => {
      b.y += b.vy * dt;
      b.x += Math.sin(b.phase + worldTime * 2) * 10 * dt;
      if (b.y + b.r < 0) { b.y = LOGICAL_H + b.r; b.x = rand(0, LOGICAL_W); }
    });
    stars.forEach(s => {
      s.y += s.vy * dt;
      s.twinkle += dt * 3;
      if (s.y > LOGICAL_H) { s.y = -10; s.x = rand(0, LOGICAL_W); }
    });
  }

  function renderBackground() {
    // Deep water gradient
    const grad = ctx.createLinearGradient(0, 0, 0, LOGICAL_H);
    grad.addColorStop(0, '#0a1628');
    grad.addColorStop(0.5, '#0d2440');
    grad.addColorStop(1, '#0a1a30');
    ctx.fillStyle = grad;
    ctx.fillRect(0, 0, LOGICAL_W, LOGICAL_H);

    // Light rays
    ctx.save();
    ctx.globalAlpha = 0.08;
    ctx.fillStyle = '#a8d4e6';
    for (let i = 0; i < 5; i++) {
      const lx = (i * 280 - (worldTime * 20) % 280) + 100;
      ctx.beginPath();
      ctx.moveTo(lx, 0);
      ctx.lineTo(lx + 80, 0);
      ctx.lineTo(lx + 150, LOGICAL_H);
      ctx.lineTo(lx + 70, LOGICAL_H);
      ctx.closePath(); ctx.fill();
    }
    ctx.restore();

    // Star motes (back layer)
    stars.forEach(s => {
      const alpha = 0.3 + 0.5 * Math.abs(Math.sin(s.twinkle));
      ctx.fillStyle = `hsla(${s.hue}, 60%, 85%, ${alpha})`;
      ctx.beginPath(); ctx.arc(s.x, s.y, s.r, 0, Math.PI * 2); ctx.fill();
    });

    // Seaweed layers
    bgLayers.forEach((layer, idx) => {
      ctx.fillStyle = layer.color;
      layer.elements.forEach(e => {
        const sway = Math.sin(worldTime * 1.5 + e.phase) * 8;
        ctx.beginPath();
        ctx.moveTo(e.x, GROUND_Y);
        ctx.bezierCurveTo(e.x + sway, e.y + e.h * 0.6, e.x + e.w + sway, e.y + e.h * 0.3, e.x + e.w * 0.5 + sway, e.y);
        ctx.bezierCurveTo(e.x + e.w + sway, e.y + e.h * 0.3, e.x + e.w + sway, e.y + e.h * 0.7, e.x + e.w, GROUND_Y);
        ctx.closePath(); ctx.fill();
      });
    });

    // Ground
    const gg = ctx.createLinearGradient(0, GROUND_Y, 0, LOGICAL_H);
    gg.addColorStop(0, '#1a3050');
    gg.addColorStop(1, '#0a1a30');
    ctx.fillStyle = gg;
    ctx.fillRect(0, GROUND_Y, LOGICAL_W, LOGICAL_H - GROUND_Y);
    ctx.strokeStyle = TEAL;
    ctx.lineWidth = 2;
    ctx.globalAlpha = 0.5;
    ctx.beginPath();
    for (let x = 0; x < LOGICAL_W; x += 20) {
      const y = GROUND_Y + Math.sin(x * 0.05 + worldTime) * 2;
      if (x === 0) ctx.moveTo(x, y); else ctx.lineTo(x, y);
    }
    ctx.stroke();
    ctx.globalAlpha = 1;

    // Bubbles
    bubbles.forEach(b => {
      ctx.strokeStyle = `rgba(180, 230, 255, 0.5)`;
      ctx.lineWidth = 1.5;
      ctx.beginPath(); ctx.arc(b.x, b.y, b.r, 0, Math.PI * 2); ctx.stroke();
      ctx.fillStyle = `rgba(255, 255, 255, 0.3)`;
      ctx.beginPath(); ctx.arc(b.x - b.r * 0.3, b.y - b.r * 0.3, b.r * 0.25, 0, Math.PI * 2); ctx.fill();
    });
  }

  // ─── Obstacles (coral) ───────────────────────────────────────────────────
  const corals = [];
  let coralTimer = 0;
  // Max coral height is capped well below the max jump arc so every coral
  // is guaranteed clearable even at top game speed.
  const MAX_CORAL_H = 150;
  const MIN_CORAL_H = 60;
  function spawnCoral() {
    const type = Math.floor(Math.random() * 3);
    // All types clamped to the guaranteed-jumpable range.
    let h;
    if (type === 2)      h = rand(90, MAX_CORAL_H);   // cluster — previously up to 170
    else if (type === 0) h = rand(75, 135);             // spire
    else                 h = rand(MIN_CORAL_H, 120);    // fan
    // Ensure horizontal spacing gives the player time to land + jump again.
    // At top speed (~800 px/s) one full jump covers ~650 px of world, so
    // back-to-back spawns need at least that much gap when both are tall.
    const lastCoral = corals[corals.length - 1];
    if (lastCoral) {
      const gap = (LOGICAL_W + 50) - (lastCoral.x + lastCoral.w);
      const bothTall = h > 110 && lastCoral.h > 110;
      const minGap = bothTall ? 480 : 280;
      if (gap < minGap) return; // skip this spawn attempt, try again next tick
    }
    corals.push({
      x: LOGICAL_W + 50,
      y: GROUND_Y - h,
      w: rand(50, 75),
      h: h,
      type: type,
      passed: false,
      nearMissChecked: false,
      nearMissFlash: 0,
    });
  }
  function updateCorals(dt, speed) {
    coralTimer -= dt;
    if (coralTimer <= 0) {
      spawnCoral();
      // Slower base spawn rate — the skip-on-too-close logic above also
      // means some tick cycles will be skipped, giving natural breathing room.
      coralTimer = rand(1.4, 2.4) / Math.min(speedScale, 1.8);
    }
    for (let i = corals.length - 1; i >= 0; i--) {
      const c = corals[i];
      c.x -= speed * dt;
      if (c.nearMissFlash > 0) c.nearMissFlash -= dt;
      if (c.x + c.w < -50) corals.splice(i, 1);
      else if (!c.passed && c.x + c.w < player.x) {
        c.passed = true;
        score += 10;
      }
    }
  }
  function renderCorals() {
    corals.forEach(c => {
      ctx.save();
      if (c.nearMissFlash > 0) {
        ctx.strokeStyle = `rgba(255, 255, 100, ${c.nearMissFlash / 0.3})`;
        ctx.lineWidth = 4;
        ctx.strokeRect(c.x - 2, c.y - 2, c.w + 4, c.h + 4);
      }
      // Base gradient
      const grad = ctx.createLinearGradient(c.x, c.y, c.x, c.y + c.h);
      if (c.type === 0) {
        grad.addColorStop(0, '#ff6b9d'); grad.addColorStop(1, '#c04070');
      } else if (c.type === 1) {
        grad.addColorStop(0, '#ff9f7a'); grad.addColorStop(1, '#c85030');
      } else {
        grad.addColorStop(0, '#c8a0ff'); grad.addColorStop(1, '#7050a0');
      }
      ctx.fillStyle = grad;
      // Shape
      if (c.type === 0) {
        // Spire with branches
        ctx.beginPath();
        ctx.moveTo(c.x + c.w * 0.3, c.y + c.h);
        ctx.lineTo(c.x + c.w * 0.5, c.y);
        ctx.lineTo(c.x + c.w * 0.7, c.y + c.h);
        ctx.closePath(); ctx.fill();
        // Branches
        ctx.strokeStyle = grad; ctx.lineWidth = 6; ctx.lineCap = 'round';
        ctx.beginPath();
        ctx.moveTo(c.x + c.w * 0.5, c.y + c.h * 0.3); ctx.lineTo(c.x + c.w * 0.15, c.y + c.h * 0.15);
        ctx.moveTo(c.x + c.w * 0.5, c.y + c.h * 0.55); ctx.lineTo(c.x + c.w * 0.85, c.y + c.h * 0.4);
        ctx.stroke();
      } else if (c.type === 1) {
        // Fan coral
        for (let i = -3; i <= 3; i++) {
          ctx.save();
          ctx.translate(c.x + c.w / 2, c.y + c.h);
          ctx.rotate((i / 3) * 0.9);
          ctx.fillRect(-3, -c.h, 6, c.h);
          ctx.restore();
        }
        ctx.beginPath();
        ctx.ellipse(c.x + c.w/2, c.y + c.h, c.w * 0.5, 6, 0, 0, Math.PI * 2);
        ctx.fill();
      } else {
        // Cluster (spheres stacked)
        const positions = [[0.5, 0.85, 0.25], [0.3, 0.55, 0.2], [0.7, 0.55, 0.2], [0.5, 0.25, 0.18]];
        positions.forEach(([rx, ry, rs]) => {
          ctx.beginPath();
          ctx.arc(c.x + c.w * rx, c.y + c.h * ry, c.w * rs, 0, Math.PI * 2);
          ctx.fill();
        });
      }
      // Outline
      ctx.strokeStyle = 'rgba(60, 20, 40, 0.5)'; ctx.lineWidth = 1.5;
      ctx.strokeRect(c.x, c.y, c.w, c.h);
      ctx.restore();
    });
  }

  // ─── Hearts ──────────────────────────────────────────────────────────────
  const heartsList = [];
  let heartTimer = 0;
  function spawnHeart() {
    const y = GROUND_Y - rand(80, 250);
    heartsList.push({
      x: LOGICAL_W + 50,
      y: y,
      baseY: y,
      collected: false,
      animTime: 0,
      golden: chance(0.15),
    });
  }
  function updateHearts(dt, speed) {
    heartTimer -= dt;
    if (heartTimer <= 0) {
      spawnHeart();
      heartTimer = rand(1.5, 3);
    }
    for (let i = heartsList.length - 1; i >= 0; i--) {
      const h = heartsList[i];
      h.x -= speed * dt;
      h.animTime += dt;
      h.y = h.baseY + Math.sin(h.animTime * 3) * 8;
      if (h.x + 40 < -50) heartsList.splice(i, 1);
    }
  }
  function renderHearts() {
    heartsList.forEach(h => {
      const bob = Math.sin(h.animTime * 3) * 4;
      const size = 20;
      if (h.golden) {
        // Orbital sparkles
        for (let i = 0; i < 6; i++) {
          const a = h.animTime * 2 + i * Math.PI / 3;
          const ox = Math.cos(a) * 25;
          const oy = Math.sin(a) * 25;
          ctx.fillStyle = `rgba(255, 215, 100, ${0.5 + 0.3 * Math.sin(h.animTime * 4 + i)})`;
          ctx.beginPath(); ctx.arc(h.x + ox, h.y + oy, 2, 0, Math.PI * 2); ctx.fill();
        }
        // Crown
        ctx.fillStyle = GOLD;
        ctx.beginPath();
        ctx.moveTo(h.x - 12, h.y - 22);
        ctx.lineTo(h.x - 8, h.y - 30);
        ctx.lineTo(h.x - 4, h.y - 24);
        ctx.lineTo(h.x, h.y - 32);
        ctx.lineTo(h.x + 4, h.y - 24);
        ctx.lineTo(h.x + 8, h.y - 30);
        ctx.lineTo(h.x + 12, h.y - 22);
        ctx.closePath(); ctx.fill();
      }
      // Glow
      const glowGrad = ctx.createRadialGradient(h.x, h.y, 5, h.x, h.y, 30);
      glowGrad.addColorStop(0, h.golden ? 'rgba(255,215,100,0.4)' : 'rgba(255,100,130,0.4)');
      glowGrad.addColorStop(1, 'rgba(0,0,0,0)');
      ctx.fillStyle = glowGrad;
      ctx.fillRect(h.x - 30, h.y - 30, 60, 60);
      // Heart
      ctx.fillStyle = h.golden ? GOLD : HEART_PINK;
      drawHeart(h.x, h.y, size);
      // Inner glow
      ctx.fillStyle = 'rgba(255, 255, 255, 0.4)';
      ctx.beginPath(); ctx.arc(h.x - 5, h.y - 8, 4, 0, Math.PI * 2); ctx.fill();
    });
  }

  // ─── Crabs (decorative ground walkers) ───────────────────────────────────
  const crabs = [];
  function initCrabs() {
    crabs.length = 0;
    for (let i = 0; i < 3; i++) {
      crabs.push({
        x: rand(0, LOGICAL_W),
        y: GROUND_Y - 8,
        vx: rand(-55, -25),          // always drifting left like the world
        legPhase: rand(0, Math.PI * 2),
        color: ['#e85a3b', '#ff8c66', '#d4432b'][i % 3],
        size: rand(18, 24),
      });
    }
  }
  function updateCrabs(dt, worldSpeed) {
    for (const c of crabs) {
      c.x += (c.vx - worldSpeed * 0.35) * dt;
      c.legPhase += dt * 12;
      if (c.x + c.size < -20) {
        c.x = LOGICAL_W + rand(20, 400);
        c.y = GROUND_Y - 8;
        c.size = rand(18, 24);
      }
    }
  }
  function renderCrabs() {
    for (const c of crabs) {
      const s = c.size;
      const cx = c.x, cy = c.y;
      // 4 legs per side, scuttle animation
      ctx.strokeStyle = '#6b2418';
      ctx.lineWidth = 2;
      ctx.lineCap = 'round';
      for (let i = 0; i < 4; i++) {
        const t = i / 3 - 0.5;
        const legLift = Math.sin(c.legPhase + i * 0.9) * 4;
        // Left legs
        ctx.beginPath();
        ctx.moveTo(cx - s * 0.2 + t * s * 0.4, cy - s * 0.3);
        ctx.lineTo(cx - s * 0.55 + t * s * 0.3, cy + 4 - legLift * 0.5);
        ctx.lineTo(cx - s * 0.75 + t * s * 0.3, cy + 6);
        ctx.stroke();
        // Right legs
        ctx.beginPath();
        ctx.moveTo(cx + s * 0.2 - t * s * 0.4, cy - s * 0.3);
        ctx.lineTo(cx + s * 0.55 - t * s * 0.3, cy + 4 - legLift * 0.5);
        ctx.lineTo(cx + s * 0.75 - t * s * 0.3, cy + 6);
        ctx.stroke();
      }
      // Body (shell)
      const bodyGrad = ctx.createRadialGradient(cx - s * 0.2, cy - s * 0.5, s * 0.1, cx, cy - s * 0.3, s * 0.7);
      bodyGrad.addColorStop(0, '#ffb088');
      bodyGrad.addColorStop(0.5, c.color);
      bodyGrad.addColorStop(1, '#6b2418');
      ctx.fillStyle = bodyGrad;
      ctx.beginPath();
      ctx.ellipse(cx, cy - s * 0.35, s * 0.65, s * 0.5, 0, 0, Math.PI * 2);
      ctx.fill();
      ctx.strokeStyle = '#6b2418';
      ctx.lineWidth = 1.5;
      ctx.stroke();
      // Claws (two small circles out front)
      ctx.fillStyle = c.color;
      ctx.beginPath();
      ctx.arc(cx - s * 0.75, cy - s * 0.55, s * 0.18, 0, Math.PI * 2);
      ctx.fill();
      ctx.stroke();
      ctx.beginPath();
      ctx.arc(cx + s * 0.75, cy - s * 0.55, s * 0.18, 0, Math.PI * 2);
      ctx.fill();
      ctx.stroke();
      // Eyes (stalked)
      ctx.strokeStyle = '#6b2418';
      ctx.lineWidth = 1.5;
      ctx.beginPath();
      ctx.moveTo(cx - s * 0.2, cy - s * 0.6);
      ctx.lineTo(cx - s * 0.2, cy - s * 0.85);
      ctx.moveTo(cx + s * 0.2, cy - s * 0.6);
      ctx.lineTo(cx + s * 0.2, cy - s * 0.85);
      ctx.stroke();
      ctx.fillStyle = '#000';
      ctx.beginPath(); ctx.arc(cx - s * 0.2, cy - s * 0.9, 2.2, 0, Math.PI * 2); ctx.fill();
      ctx.beginPath(); ctx.arc(cx + s * 0.2, cy - s * 0.9, 2.2, 0, Math.PI * 2); ctx.fill();
    }
  }

  // ─── Shield bubbles (collectible) ────────────────────────────────────────
  const shieldBubbles = [];
  let shieldTimer = 0;
  function spawnShieldBubble() {
    shieldBubbles.push({
      x: LOGICAL_W + 40,
      y: GROUND_Y - rand(110, 240),
      baseY: 0,
      r: 26,
      animTime: 0,
      collected: false,
    });
    shieldBubbles[shieldBubbles.length - 1].baseY = shieldBubbles[shieldBubbles.length - 1].y;
  }
  function updateShieldBubbles(dt, speed) {
    shieldTimer -= dt;
    // Rare — one roughly every 12-18 seconds, and never if player already has one
    if (shieldTimer <= 0) {
      if (!player.hasShield) spawnShieldBubble();
      shieldTimer = rand(12, 18);
    }
    for (let i = shieldBubbles.length - 1; i >= 0; i--) {
      const s = shieldBubbles[i];
      s.x -= speed * dt;
      s.animTime += dt;
      s.y = s.baseY + Math.sin(s.animTime * 2.5) * 6;
      if (s.x + s.r < -50) shieldBubbles.splice(i, 1);
    }
  }
  function renderShieldBubbles() {
    for (const s of shieldBubbles) {
      // Glow
      const glow = ctx.createRadialGradient(s.x, s.y, s.r * 0.3, s.x, s.y, s.r * 1.8);
      glow.addColorStop(0, 'rgba(130, 210, 255, 0.4)');
      glow.addColorStop(1, 'rgba(130, 210, 255, 0)');
      ctx.fillStyle = glow;
      ctx.beginPath(); ctx.arc(s.x, s.y, s.r * 1.8, 0, Math.PI * 2); ctx.fill();
      // Bubble body
      const bg = ctx.createRadialGradient(s.x - s.r * 0.3, s.y - s.r * 0.3, s.r * 0.1, s.x, s.y, s.r);
      bg.addColorStop(0, 'rgba(240, 250, 255, 0.6)');
      bg.addColorStop(0.6, 'rgba(140, 210, 255, 0.4)');
      bg.addColorStop(1, 'rgba(80, 170, 230, 0.5)');
      ctx.fillStyle = bg;
      ctx.beginPath(); ctx.arc(s.x, s.y, s.r, 0, Math.PI * 2); ctx.fill();
      ctx.strokeStyle = 'rgba(200, 240, 255, 0.9)';
      ctx.lineWidth = 2;
      ctx.beginPath(); ctx.arc(s.x, s.y, s.r, 0, Math.PI * 2); ctx.stroke();
      // Highlight
      ctx.fillStyle = 'rgba(255, 255, 255, 0.7)';
      ctx.beginPath();
      ctx.ellipse(s.x - s.r * 0.35, s.y - s.r * 0.4, s.r * 0.18, s.r * 0.1, -0.5, 0, Math.PI * 2);
      ctx.fill();
      // Orbital sparkles
      for (let i = 0; i < 3; i++) {
        const a = s.animTime * 2 + i * (Math.PI * 2 / 3);
        const ox = Math.cos(a) * (s.r + 6);
        const oy = Math.sin(a) * (s.r + 6);
        ctx.fillStyle = `rgba(200, 240, 255, ${0.5 + 0.3 * Math.sin(s.animTime * 3 + i)})`;
        ctx.beginPath(); ctx.arc(s.x + ox, s.y + oy, 2, 0, Math.PI * 2); ctx.fill();
      }
    }
  }

  // ─── Particles ───────────────────────────────────────────────────────────
  const particles = [];
  function spawnParticleBurst(x, y, color, count) {
    for (let i = 0; i < count; i++) {
      particles.push({
        x, y,
        vx: rand(-150, 150), vy: rand(-200, -50),
        life: rand(0.4, 0.8), maxLife: 0.8,
        size: rand(3, 6), color,
      });
    }
  }
  function updateParticles(dt) {
    for (let i = particles.length - 1; i >= 0; i--) {
      const p = particles[i];
      p.life -= dt;
      if (p.life <= 0) { particles.splice(i, 1); continue; }
      p.x += p.vx * dt;
      p.y += p.vy * dt;
      p.vy += 400 * dt;
    }
  }
  function renderParticles() {
    particles.forEach(p => {
      ctx.globalAlpha = p.life / p.maxLife;
      ctx.fillStyle = p.color;
      ctx.beginPath(); ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2); ctx.fill();
    });
    ctx.globalAlpha = 1;
  }

  // ─── Floating text (score pops, blup) ────────────────────────────────────
  const floats = [];
  function spawnFloat(text, x, y, color, size, bubble = false) {
    floats.push({ text, x, y, vy: -60, life: 1, maxLife: 1, color, size, bubble });
  }
  function spawnBlup(x, y) {
    spawnFloat('blup!', x, y, '#b5eaf7', 22, true);
  }
  function updateFloats(dt) {
    for (let i = floats.length - 1; i >= 0; i--) {
      const f = floats[i];
      f.life -= dt / f.maxLife;
      f.y += f.vy * dt;
      f.vy *= 0.96;
      if (f.life <= 0) floats.splice(i, 1);
    }
  }
  function renderFloats() {
    floats.forEach(f => {
      ctx.globalAlpha = f.life;
      if (f.bubble) {
        const w = ctx.measureText(f.text).width + 16;
        ctx.font = `bold ${f.size}px -apple-system, sans-serif`;
        const tw = ctx.measureText(f.text).width + 16;
        ctx.fillStyle = 'rgba(200, 240, 255, 0.9)';
        ctx.beginPath();
        if (ctx.roundRect) ctx.roundRect(f.x - 8, f.y - f.size, tw, f.size + 10, 8);
        else ctx.rect(f.x - 8, f.y - f.size, tw, f.size + 10);
        ctx.fill();
      }
      ctx.font = `bold ${f.size}px -apple-system, sans-serif`;
      ctx.fillStyle = 'rgba(0, 0, 0, 0.5)';
      ctx.fillText(f.text, f.x + 2, f.y + 2);
      ctx.fillStyle = f.color;
      ctx.fillText(f.text, f.x, f.y);
    });
    ctx.globalAlpha = 1;
  }

  // ─── Collision ───────────────────────────────────────────────────────────
  function rectCollide(a, b) {
    return a.x < b.x + b.w && a.x + a.width > b.x &&
           a.y < b.y + b.h && a.y + a.height > b.y;
  }
  function checkCollisions() {
    // Shield bubble pickup first (so you can grab one mid-jump into a coral)
    for (let i = shieldBubbles.length - 1; i >= 0; i--) {
      const s = shieldBubbles[i];
      const dx = (player.x + player.width / 2) - s.x;
      const dy = (player.y + player.height / 2) - s.y;
      if (Math.hypot(dx, dy) < s.r + 30) {
        shieldBubbles.splice(i, 1);
        player.hasShield = true;
        player.shieldPulse = 0;
        sfx.collect();
        spawnParticleBurst(s.x, s.y, '#a8dcff', 14);
        spawnFloat('SHIELD!', s.x - 30, s.y - 20, '#a8dcff', 22);
      }
    }

    // Coral hits + near misses
    for (const c of corals) {
      const shrunkA = { x: player.x + 15, y: player.y + 10, width: player.width - 30, height: player.height - 20 };
      if (rectCollide(shrunkA, c)) {
        if (player.hasShield) {
          // Absorb the hit, consume the shield, punch the coral away
          player.hasShield = false;
          c.x -= 200; // shove the coral offscreen-ish so we don't re-collide next frame
          spawnParticleBurst(player.x + player.width / 2, player.y + player.height / 2, '#a8dcff', 20);
          spawnFloat('SHIELD BROKE!', player.x, player.y - 30, '#a8dcff', 22);
          cameraShake = 8;
          sfx.hit();
          return;
        }
        die();
        return;
      }
      // Near miss: player just cleared coral and vertical distance small
      if (!c.nearMissChecked && player.x > c.x + c.w) {
        c.nearMissChecked = true;
        const playerBottom = player.y + player.height;
        const coralTop = c.y;
        if (playerBottom < coralTop && coralTop - playerBottom < 35) {
          c.nearMissFlash = 0.3;
          score += 5;
          spawnFloat('+NEAR MISS!', player.x + 60, player.y - 20, TEAL, 18);
          spawnParticleBurst(c.x + c.w / 2, c.y, TEAL, 6);
        }
      }
    }
    // Heart collection
    for (const h of heartsList) {
      if (h.collected) continue;
      const hRect = { x: h.x - 20, y: h.y - 20, w: 40, h: 40 };
      if (rectCollide({ x: player.x, y: player.y, width: player.width, height: player.height }, hRect)) {
        h.collected = true;
        hearts++;
        chain++;
        chainTimer = 3;
        maxChain = Math.max(maxChain, chain);
        const pts = h.golden ? 30 : 10;
        const totalPts = Math.floor(pts * (1 + chain * 0.1));
        score += totalPts;
        sfx.collect();
        if (chain > 2) sfx.chain(chain);
        spawnParticleBurst(h.x, h.y, h.golden ? GOLD : HEART_PINK, h.golden ? 20 : 10);
        spawnFloat(`+${totalPts}`, h.x, h.y - 10, h.golden ? GOLD : HEART_PINK, 22);
        if (h.golden) {
          spawnFloat('✦ GOLDEN! ✦', h.x - 40, h.y - 45, GOLD, 24);
          player.setHappy(1.0);
        } else if (chain >= 5) {
          player.setHappy(0.8);
        }
        // Remove collected heart
        heartsList.splice(heartsList.indexOf(h), 1);
        break;
      }
    }
  }

  function die() {
    sfx.hit();
    sfx.gameover();
    state = STATE_GAMEOVER;
    player.expression = 'dead';
    cameraShake = 20;
    spawnParticleBurst(player.x + player.width / 2, player.y + player.height / 2, '#ff4060', 25);
    if (score > bestScore) {
      bestScore = score;
      localStorage.setItem('coralDriftBest', bestScore);
    }
  }

  // ─── HUD ─────────────────────────────────────────────────────────────────
  function renderHUD() {
    ctx.save();
    ctx.font = 'bold 32px -apple-system, sans-serif';
    ctx.fillStyle = 'rgba(0, 0, 0, 0.5)';
    ctx.fillText(`Score: ${score}`, 32, 52);
    ctx.fillStyle = GOLD;
    ctx.fillText(`Score: ${score}`, 30, 50);

    ctx.font = 'bold 24px -apple-system, sans-serif';
    ctx.fillStyle = 'rgba(0, 0, 0, 0.5)';
    ctx.fillText(`♥ ${hearts}`, 32, 92);
    ctx.fillStyle = HEART_PINK;
    ctx.fillText(`♥ ${hearts}`, 30, 90);

    if (chain > 1) {
      ctx.font = 'bold 28px -apple-system, sans-serif';
      const chainColor = chain >= 5 ? GOLD : TEAL;
      ctx.fillStyle = chainColor;
      ctx.fillText(`x${chain} CHAIN!`, 30, 130);
    }

    // Best score top-right
    ctx.textAlign = 'right';
    ctx.font = 'bold 18px -apple-system, sans-serif';
    ctx.fillStyle = 'rgba(255,255,255,0.6)';
    ctx.fillText(`Best: ${bestScore}`, LOGICAL_W - 30, 40);
    ctx.textAlign = 'left';
    ctx.restore();
  }

  // ─── Screens ─────────────────────────────────────────────────────────────
  function renderMenu() {
    ctx.save();
    ctx.textAlign = 'center';
    ctx.font = 'bold 72px -apple-system, sans-serif';
    const titleY = 180 + Math.sin(worldTime * 2) * 8;
    // Title shadow
    ctx.fillStyle = 'rgba(0, 0, 0, 0.6)';
    ctx.fillText('Coral Drift', LOGICAL_W / 2 + 4, titleY + 4);
    // Gradient title
    const titleGrad = ctx.createLinearGradient(0, titleY - 50, 0, titleY + 10);
    titleGrad.addColorStop(0, '#ffd4e8');
    titleGrad.addColorStop(1, PINK);
    ctx.fillStyle = titleGrad;
    ctx.fillText('Coral Drift', LOGICAL_W / 2, titleY);

    // Octopus mascot
    drawOctopus(LOGICAL_W / 2 - 60, 250, 120, 110);

    // Tap to play
    const pulse = 0.7 + 0.3 * Math.sin(worldTime * 3);
    ctx.font = 'bold 36px -apple-system, sans-serif';
    ctx.fillStyle = `rgba(255, 255, 255, ${pulse})`;
    ctx.fillText('TAP TO PLAY', LOGICAL_W / 2, 480);

    ctx.font = '20px -apple-system, sans-serif';
    ctx.fillStyle = 'rgba(255, 255, 255, 0.6)';
    ctx.fillText('Tap or press SPACE to jump', LOGICAL_W / 2, 520);
    ctx.fillText(`Best Score: ${bestScore}`, LOGICAL_W / 2, 550);

    ctx.textAlign = 'left';
    ctx.restore();
  }

  let gameOverTime = 0;
  function renderGameOver() {
    ctx.save();
    // Dark overlay
    ctx.fillStyle = `rgba(0, 0, 0, ${Math.min(0.6, gameOverTime)})`;
    ctx.fillRect(0, 0, LOGICAL_W, LOGICAL_H);

    if (gameOverTime < 0.3) { ctx.restore(); return; }

    ctx.textAlign = 'center';
    // Panel
    const panelW = 500; const panelH = 380;
    const panelX = (LOGICAL_W - panelW) / 2;
    const panelY = (LOGICAL_H - panelH) / 2 - (1 - Math.min(1, (gameOverTime - 0.3) / 0.4)) * LOGICAL_H;
    ctx.fillStyle = 'rgba(20, 35, 60, 0.95)';
    if (ctx.roundRect) {
      ctx.beginPath(); ctx.roundRect(panelX, panelY, panelW, panelH, 20); ctx.fill();
    } else {
      ctx.fillRect(panelX, panelY, panelW, panelH);
    }
    ctx.strokeStyle = TEAL;
    ctx.lineWidth = 3;
    if (ctx.roundRect) {
      ctx.beginPath(); ctx.roundRect(panelX, panelY, panelW, panelH, 20); ctx.stroke();
    } else ctx.strokeRect(panelX, panelY, panelW, panelH);

    // Title
    ctx.font = 'bold 56px -apple-system, sans-serif';
    ctx.fillStyle = HEART_PINK;
    ctx.fillText('Game Over', LOGICAL_W / 2, panelY + 80);

    // Count-up score
    const t = Math.min(1, (gameOverTime - 0.6) / 1.5);
    const eased = 1 - Math.pow(1 - t, 3);
    const displayScore = Math.floor(score * eased);
    const displayHearts = Math.floor(hearts * eased);

    ctx.font = 'bold 28px -apple-system, sans-serif';
    ctx.fillStyle = '#a8d4e6';
    ctx.fillText('Score', LOGICAL_W / 2, panelY + 140);
    ctx.font = 'bold 42px -apple-system, sans-serif';
    ctx.fillStyle = GOLD;
    ctx.fillText(`${displayScore}`, LOGICAL_W / 2, panelY + 185);

    if (score === bestScore && score > 0) {
      ctx.font = 'bold 22px -apple-system, sans-serif';
      ctx.fillStyle = GOLD;
      ctx.fillText('★ NEW BEST! ★', LOGICAL_W / 2, panelY + 215);
    }

    ctx.font = 'bold 24px -apple-system, sans-serif';
    ctx.fillStyle = HEART_PINK;
    ctx.fillText(`♥ ${displayHearts}   Best Chain: x${maxChain}`, LOGICAL_W / 2, panelY + 255);

    // Tap to restart
    if (gameOverTime > 2) {
      const pulse = 0.6 + 0.4 * Math.sin(worldTime * 3);
      ctx.font = 'bold 28px -apple-system, sans-serif';
      ctx.fillStyle = `rgba(255, 255, 255, ${pulse})`;
      ctx.fillText('TAP TO PLAY AGAIN', LOGICAL_W / 2, panelY + 330);
    }

    ctx.textAlign = 'left';
    ctx.restore();
  }

  // ─── Main loop ───────────────────────────────────────────────────────────
  let lastTime = 0;
  function loop(now) {
    if (!lastTime) lastTime = now;
    let dt = (now - lastTime) / 1000;
    lastTime = now;
    dt = Math.min(dt, 1/30); // clamp to avoid huge jumps

    worldTime += dt;
    update(dt);
    render();
    requestAnimationFrame(loop);
  }

  function update(dt) {
    if (state === STATE_PLAYING) {
      speedScale = 1 + Math.min(1.5, score / 400);
      const speed = BASE_SPEED * speedScale;
      player.update(dt);
      updateCorals(dt, speed);
      updateHearts(dt, speed);
      updateShieldBubbles(dt, speed);
      updateCrabs(dt, speed);
      updateBackground(dt, speed);
      updateParticles(dt);
      updateFloats(dt);
      checkCollisions();
      if (chainTimer > 0) {
        chainTimer -= dt;
        if (chainTimer <= 0) chain = 0;
      }
    } else if (state === STATE_GAMEOVER) {
      gameOverTime += dt;
      updateBackground(dt, BASE_SPEED * 0.3);
      updateCorals(dt, BASE_SPEED * 0.3);
      updateHearts(dt, BASE_SPEED * 0.3);
      updateShieldBubbles(dt, BASE_SPEED * 0.3);
      updateCrabs(dt, BASE_SPEED * 0.3);
      updateParticles(dt);
      updateFloats(dt);
      player.update(dt);
    } else {
      // Menu
      player.update(dt);
      updateBackground(dt, BASE_SPEED * 0.3);
      updateCrabs(dt, BASE_SPEED * 0.3);
    }
    if (cameraShake > 0) cameraShake *= 0.9;
  }

  function render() {
    ctx.save();
    ctx.translate(offsetX, offsetY);
    ctx.scale(scale, scale);

    // Clip to logical bounds so letterbox stays clean
    ctx.beginPath();
    ctx.rect(0, 0, LOGICAL_W, LOGICAL_H);
    ctx.clip();

    // Camera shake
    if (cameraShake > 0.1) {
      ctx.translate(rand(-cameraShake, cameraShake), rand(-cameraShake, cameraShake));
    }

    renderBackground();
    renderCrabs();  // behind everything else, ground level

    if (state === STATE_MENU) {
      renderCorals();
      renderHearts();
      renderShieldBubbles();
      renderParticles();
      renderMenu();
    } else {
      renderCorals();
      renderHearts();
      renderShieldBubbles();
      drawOctopus(player.x, player.y, player.width, player.height);
      renderParticles();
      renderFloats();
      renderHUD();
      if (state === STATE_GAMEOVER) renderGameOver();
    }

    ctx.restore();
  }

  // ─── Input ───────────────────────────────────────────────────────────────
  function startGame() {
    initAudio();
    state = STATE_PLAYING;
    score = 0; hearts = 0; chain = 0; maxChain = 0; chainTimer = 0;
    speedScale = 1; cameraShake = 0; gameOverTime = 0;
    corals.length = 0;
    heartsList.length = 0;
    shieldBubbles.length = 0;
    particles.length = 0;
    floats.length = 0;
    coralTimer = 1.5;
    heartTimer = 2;
    shieldTimer = 8; // first shield bubble appears ~8s in
    player.reset();
    initCrabs();
  }

  function handleInput() {
    if (state === STATE_MENU) startGame();
    else if (state === STATE_PLAYING) player.jump();
    else if (state === STATE_GAMEOVER && gameOverTime > 2) startGame();
  }

  window.addEventListener('keydown', (e) => {
    if (e.code === 'Space' || e.code === 'ArrowUp' || e.code === 'KeyW') {
      e.preventDefault();
      handleInput();
    }
  });
  canvas.addEventListener('mousedown', (e) => { e.preventDefault(); handleInput(); });
  canvas.addEventListener('touchstart', (e) => { e.preventDefault(); handleInput(); }, { passive: false });

  // ─── Boot ────────────────────────────────────────────────────────────────
  initBackground();
  initCrabs();
  loading.style.display = 'none';
  requestAnimationFrame(loop);
})();
