import { useState, useEffect } from "react";

const API = "https://shorten-url-2-juwn.onrender.com";

/* ── helpers ──────────────────────────────────────────── */
function useToken() {
  const [token, setToken] = useState(() => localStorage.getItem("ss_token"));
  const save = (t) => {
    localStorage.setItem("ss_token", t);
    setToken(t);
  };
  const clear = () => {
    localStorage.removeItem("ss_token");
    setToken(null);
  };
  return [token, save, clear];
}

function authHeaders(token) {
  return {
    "Content-Type": "application/json",
    Authorization: `Bearer ${token}`,
  };
}

/* ── shared components ────────────────────────────────── */
function Msg({ type, text }) {
  if (!text) return null;
  return (
    <p
      className={`text-xs px-3 py-2 border-l-2 font-mono ${
        type === "err"
          ? "border-red-500 bg-red-50 text-red-600"
          : "border-green-500 bg-green-50 text-green-700"
      }`}
    >
      {text}
    </p>
  );
}

function Field({ label, children }) {
  return (
    <div className="flex flex-col gap-1">
      <label className="text-[10px] uppercase tracking-widest text-stone-400 font-medium">
        {label}
      </label>
      {children}
    </div>
  );
}

function Input(props) {
  return (
    <input
      {...props}
      className="bg-stone-50 border border-stone-200 px-3 py-2 text-sm text-stone-800
                 font-mono outline-none focus:border-stone-700 transition-colors
                 placeholder:text-stone-300 w-full"
    />
  );
}

function Btn({ children, variant = "primary", className = "", ...props }) {
  const base =
    "text-xs font-mono tracking-wide px-4 py-2.5 transition-colors cursor-pointer disabled:opacity-40 disabled:cursor-not-allowed";
  const v = {
    primary: "bg-stone-800 text-stone-50 hover:bg-red-700",
    ghost:
      "border border-stone-300 text-stone-600 hover:bg-stone-800 hover:text-stone-50 hover:border-stone-800",
  };
  return (
    <button {...props} className={`${base} ${v[variant]} ${className}`}>
      {children}
    </button>
  );
}

function ResultRow({ url }) {
  const [copied, setCopied] = useState(false);
  const copy = () => {
    navigator.clipboard.writeText(url);
    setCopied(true);
    setTimeout(() => setCopied(false), 1500);
  };
  return (
    <div className="flex items-center justify-between gap-3 bg-stone-50 border border-stone-200 px-3 py-2.5">
      <a
        href={url}
        target="_blank"
        rel="noreferrer"
        className="text-sm font-mono text-red-700 hover:underline truncate"
      >
        {url}
      </a>
      <button
        onClick={copy}
        className="text-[10px] font-mono border border-stone-300 px-2 py-1 text-stone-500
                         hover:bg-stone-800 hover:text-stone-50 hover:border-stone-800
                         transition-colors whitespace-nowrap"
      >
        {copied ? "✓ copied" : "copy"}
      </button>
    </div>
  );
}

/* ── Auth ─────────────────────────────────────────────── */
function AuthPanel({ onToken }) {
  const [mode, setMode] = useState("login");
  const [email, setEmail] = useState("");
  const [password, setPass] = useState("");
  const [err, setErr] = useState(null);
  const [loading, setLoading] = useState(false);

  const submit = async () => {
    setLoading(true);
    setErr(null);
    try {
      const res = await fetch(`${API}/auth/${mode}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });
      const text = await res.text();
      if (!res.ok) throw new Error(text || res.statusText);
      onToken(text.replace(/^"|"$/g, ""));
    } catch (e) {
      setErr(e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="border border-stone-200 bg-white">
      {/* mode tabs */}
      <div className="flex border-b border-stone-200">
        {[
          ["login", "Sign in"],
          ["register", "Register"],
        ].map(([id, label]) => (
          <button
            key={id}
            onClick={() => {
              setMode(id);
              setErr(null);
            }}
            className={`flex-1 py-3 text-xs font-mono tracking-wide transition-colors
                    ${
                      mode === id
                        ? "border-b-2 border-red-600 text-stone-800 -mb-px"
                        : "text-stone-400 hover:text-stone-600"
                    }`}
          >
            {label}
          </button>
        ))}
      </div>

      <div className="p-6 flex flex-col gap-4">
        <Field label="Email">
          <Input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="you@example.com"
            onKeyDown={(e) => e.key === "Enter" && submit()}
          />
        </Field>

        <Field label="Password">
          <Input
            type="password"
            value={password}
            onChange={(e) => setPass(e.target.value)}
            placeholder="••••••••"
            onKeyDown={(e) => e.key === "Enter" && submit()}
          />
        </Field>

        <Msg type="err" text={err} />

        <Btn onClick={submit} disabled={loading} className="w-full">
          {loading
            ? "..."
            : mode === "login"
              ? "Sign in →"
              : "Create account →"}
        </Btn>

        {/* divider */}
        <div className="flex items-center gap-3 text-[10px] text-stone-400 tracking-widest uppercase">
          <span className="flex-1 h-px bg-stone-200" /> or{" "}
          <span className="flex-1 h-px bg-stone-200" />
        </div>

        {/* Google OAuth */}
        <button
          onClick={() =>
            (window.location.href = `${API}/oauth2/authorization/google`)
          }
          className="flex items-center justify-center gap-2.5 border border-stone-200 py-2.5
                           text-xs font-mono text-stone-600 hover:bg-stone-800 hover:text-stone-50
                           hover:border-stone-800 transition-colors"
        >
          <svg width="14" height="14" viewBox="0 0 18 18">
            <path
              fill="#4285F4"
              d="M17.64 9.2c0-.637-.057-1.251-.164-1.84H9v3.481h4.844c-.209 1.125-.843 2.078-1.796 2.717v2.258h2.908c1.702-1.567 2.684-3.875 2.684-6.615z"
            />
            <path
              fill="#34A853"
              d="M9 18c2.43 0 4.467-.806 5.956-2.18l-2.908-2.259c-.806.54-1.837.86-3.048.86-2.344 0-4.328-1.584-5.036-3.711H.957v2.332C2.438 15.983 5.482 18 9 18z"
            />
            <path
              fill="#FBBC05"
              d="M3.964 10.71c-.18-.54-.282-1.117-.282-1.71s.102-1.17.282-1.71V4.958H.957C.347 6.173 0 7.548 0 9s.348 2.827.957 4.042l3.007-2.332z"
            />
            <path
              fill="#EA4335"
              d="M9 3.58c1.321 0 2.508.454 3.44 1.345l2.582-2.58C13.463.891 11.426 0 9 0 5.482 0 2.438 2.017.957 4.958L3.964 7.29C4.672 5.163 6.656 3.58 9 3.58z"
            />
          </svg>
          Continue with Google
        </button>
      </div>
    </div>
  );
}

/* ── Shorten ──────────────────────────────────────────── */
function ShortenPanel({ token }) {
  const [url, setUrl] = useState("");
  const [expiry, setExpiry] = useState("");
  const [result, setResult] = useState(null);
  const [err, setErr] = useState(null);
  const [loading, setLoading] = useState(false);

  const shorten = async () => {
    if (!url) return;
    setLoading(true);
    setErr(null);
    setResult(null);
    try {
      const res = await fetch(`${API}/api/shorten`, {
        method: "POST",
        headers: authHeaders(token),
        body: JSON.stringify({
          longUrl: url,
          expirationMinutes: expiry ? Number(expiry) : null,
        }),
      });
      const text = await res.text();
      if (!res.ok) throw new Error(text || res.statusText);
      setResult(text.replace(/^"|"$/g, ""));
      setUrl("");
      setExpiry("");
    } catch (e) {
      setErr(e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="border border-stone-200 bg-white p-6 flex flex-col gap-4">
      <Field label="Long URL">
        <Input
          value={url}
          onChange={(e) => setUrl(e.target.value)}
          placeholder="https://example.com/very/long/url"
          onKeyDown={(e) => e.key === "Enter" && shorten()}
        />
      </Field>

      <Field label="Expiry in minutes — optional">
        <Input
          type="number"
          min="1"
          value={expiry}
          onChange={(e) => setExpiry(e.target.value)}
          placeholder="default"
        />
      </Field>

      <Msg type="err" text={err} />

      <Btn onClick={shorten} disabled={loading || !url}>
        {loading ? "..." : "Shorten →"}
      </Btn>

      {result && <ResultRow url={result} />}
    </div>
  );
}

/* ── Update expiry ────────────────────────────────────── */
function UpdatePanel({ token }) {
  const [code, setCode] = useState("");
  const [mins, setMins] = useState("");
  const [result, setResult] = useState(null);
  const [msg, setMsg] = useState(null);
  const [loading, setLoading] = useState(false);

  const update = async () => {
    if (!code || !mins) return;
    setLoading(true);
    setMsg(null);
    setResult(null);
    try {
      const res = await fetch(
        `${API}/api/urls/${code}/expiration?minutes=${mins}`,
        {
          method: "PATCH",
          headers: authHeaders(token),
        },
      );
      const text = await res.text();
      if (!res.ok) throw new Error(text || res.statusText);
      setResult(text.replace(/^"|"$/g, ""));
      setMsg({ type: "ok", text: "Expiration updated." });
    } catch (e) {
      setMsg({ type: "err", text: e.message });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="border border-stone-200 bg-white p-6 flex flex-col gap-4">
      <div className="flex gap-3">
        <Field label="Short code">
          <Input
            value={code}
            onChange={(e) => setCode(e.target.value)}
            placeholder="aB3x"
          />
        </Field>
        <Field label="New expiry (min)">
          <Input
            type="number"
            min="1"
            value={mins}
            onChange={(e) => setMins(e.target.value)}
            placeholder="60"
          />
        </Field>
      </div>

      <Msg type={msg?.type} text={msg?.text} />
      {result && <ResultRow url={result} />}

      <Btn onClick={update} disabled={loading || !code || !mins}>
        {loading ? "..." : "Update →"}
      </Btn>
    </div>
  );
}

/* ── App ──────────────────────────────────────────────── */
export default function App() {
  const [token, saveToken, clearToken] = useToken();
  const [tab, setTab] = useState("shorten");

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const t = params.get("token");
    if (t) {
      saveToken(t);
      window.history.replaceState({}, "", "/");
    }
  }, []);

  return (
    <div className="min-h-screen bg-stone-100 flex flex-col items-center py-16 px-4">
      <div className="w-full max-w-md flex flex-col gap-6">
        {/* header */}
        <div className="text-center">
          <h1 className="text-4xl font-serif tracking-tight text-stone-800">
            Short<span className="italic text-red-700">Stuff</span>
          </h1>
          <p className="text-[10px] uppercase tracking-[0.2em] text-stone-400 mt-1">
            minimal url shortener
          </p>
        </div>

        {!token ? (
          <AuthPanel onToken={saveToken} />
        ) : (
          <>
            {/* user bar */}
            <div className="flex items-center justify-between text-xs font-mono">
              <span className="text-stone-500">signed in</span>
              <Btn variant="ghost" onClick={clearToken} className="py-1 px-3">
                sign out
              </Btn>
            </div>

            {/* feature tabs */}
            <div className="flex border-b border-stone-200">
              {[
                ["shorten", "Shorten URL"],
                ["update", "Update expiry"],
              ].map(([id, label]) => (
                <button
                  key={id}
                  onClick={() => setTab(id)}
                  className={`py-2.5 px-4 text-xs font-mono tracking-wide transition-colors
                          ${
                            tab === id
                              ? "border-b-2 border-red-600 text-stone-800 -mb-px"
                              : "text-stone-400 hover:text-stone-600"
                          }`}
                >
                  {label}
                </button>
              ))}
            </div>

            {tab === "shorten" && <ShortenPanel token={token} />}
            {tab === "update" && <UpdatePanel token={token} />}
          </>
        )}
      </div>
    </div>
  );
}
