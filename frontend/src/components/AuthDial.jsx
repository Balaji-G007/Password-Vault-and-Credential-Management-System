import { useEffect, useRef } from 'react';

const GLYPHS = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ#%&$*<>{}[]01';
const PHRASES = ['ENCRYPTING', 'AES-256 · GCM', 'VAULT SECURED', 'ZERO-KNOWLEDGE'];

export default function AuthDial() {
  const cipherRef = useRef(null);
  const ticksGroupRef = useRef(null);

  useEffect(() => {
    // Build the 40 tick marks around the dial once on mount.
    const group = ticksGroupRef.current;
    if (group && group.childElementCount === 0) {
      for (let i = 0; i < 40; i++) {
        const angle = (i / 40) * 360;
        const major = i % 5 === 0;
        const r1 = major ? 78 : 84;
        const r2 = 95;
        const rad = (angle * Math.PI) / 180;
        const x1 = 110 + r1 * Math.sin(rad);
        const y1 = 110 - r1 * Math.cos(rad);
        const x2 = 110 + r2 * Math.sin(rad);
        const y2 = 110 - r2 * Math.cos(rad);
        const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
        line.setAttribute('x1', x1); line.setAttribute('y1', y1);
        line.setAttribute('x2', x2); line.setAttribute('y2', y2);
        line.setAttribute('class', 'dial-tick' + (major ? ' major' : ''));
        group.appendChild(line);
      }
    }

    // Cipher scramble-in text cycling through phrases.
    let phraseIndex = 0;
    let scrambleInterval;

    function scrambleTo(target, duration = 900) {
      const steps = 14;
      let step = 0;
      clearInterval(scrambleInterval);
      scrambleInterval = setInterval(() => {
        step++;
        let out = '';
        for (let i = 0; i < target.length; i++) {
          if (target[i] === ' ' || target[i] === '·' || target[i] === '-') { out += target[i]; continue; }
          const revealAt = (i / target.length) * steps;
          out += step >= revealAt + (steps - 4) ? target[i] : GLYPHS[Math.floor(Math.random() * GLYPHS.length)];
        }
        if (cipherRef.current) cipherRef.current.textContent = out;
        if (step >= steps) clearInterval(scrambleInterval);
      }, duration / steps);
    }

    function cycleCipher() {
      scrambleTo(PHRASES[phraseIndex]);
      phraseIndex = (phraseIndex + 1) % PHRASES.length;
    }
    cycleCipher();
    const cycleTimer = setInterval(cycleCipher, 3200);

    return () => {
      clearInterval(scrambleInterval);
      clearInterval(cycleTimer);
    };
  }, []);

  return (
    <div className="dial-wrap">
      <svg className="dial" viewBox="0 0 220 220" aria-hidden="true">
        <circle className="dial-ring" cx="110" cy="110" r="95"></circle>
        <g ref={ticksGroupRef}></g>
        <line className="dial-needle" x1="110" y1="110" x2="110" y2="34"></line>
        <circle className="dial-center" cx="110" cy="110" r="16"></circle>
      </svg>
      <div className="cipher-line" ref={cipherRef}>&nbsp;</div>
    </div>
  );
}
