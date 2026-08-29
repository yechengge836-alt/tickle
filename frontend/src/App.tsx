import { useEffect, useMemo, useState, type FormEvent } from 'react'
import {
  ArrowRight,
  CalendarDays,
  CheckCircle2,
  Clock3,
  LoaderCircle,
  LogIn,
  MapPin,
  Minus,
  Plus,
  RefreshCw,
  ShieldCheck,
  Sparkles,
  Ticket,
  UserRound,
  Users,
  X,
} from 'lucide-react'

type Activity = {
  id: number
  name: string
  totalStock: number
  availableStock: number
  status: string
  startAt: string
  endAt: string
}

type ApiResult<T> = { code: number; message: string; data: T }
type LoginUser = { id: number; username: string; createdAt: string; accessToken: string }

const demoActivity: Activity = {
  id: 1,
  name: '2026 校园文化节开幕演出',
  totalStock: 1000,
  availableStock: 1000,
  status: 'ON_SALE',
  startAt: '2026-09-15T19:00:00',
  endAt: '2026-09-15T22:00:00',
}

const toCnDate = (value: string) =>
  new Intl.DateTimeFormat('zh-CN', { month: 'long', day: 'numeric', weekday: 'short' }).format(new Date(value))

const toTime = (value: string) =>
  new Intl.DateTimeFormat('zh-CN', { hour: '2-digit', minute: '2-digit', hour12: false }).format(new Date(value))

export default function App() {
  const [activity, setActivity] = useState<Activity>(demoActivity)
  const [quantity, setQuantity] = useState(1)
  const [currentUser, setCurrentUser] = useState<LoginUser | null>(() => {
    try { return JSON.parse(localStorage.getItem('ticket-platform-user') ?? 'null') as LoginUser | null } catch { return null }
  })
  const [authOpen, setAuthOpen] = useState(false)
  const [authMode, setAuthMode] = useState<'login' | 'register'>('login')
  const [authForm, setAuthForm] = useState({ username: '', password: '' })
  const [authMessage, setAuthMessage] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [isBooking, setIsBooking] = useState(false)
  const [isDemo, setIsDemo] = useState(false)
  const [notice, setNotice] = useState('')

  const soldRate = useMemo(() => {
    if (!activity.totalStock) return 0
    return Math.min(100, Math.round(((activity.totalStock - activity.availableStock) / activity.totalStock) * 100))
  }, [activity])

  const loadActivity = async () => {
    setIsLoading(true)
    setNotice('')
    try {
      const response = await fetch('/api/v1/activities/1')
      if (!response.ok) throw new Error('接口暂时不可用')
      const result = (await response.json()) as ApiResult<Activity>
      if (result.code !== 0 || !result.data) throw new Error(result.message || '活动加载失败')
      setActivity(result.data)
      setIsDemo(false)
    } catch {
      setActivity(demoActivity)
      setIsDemo(true)
      setNotice('后端暂未连接，正在展示可交互的演示数据。启动后端后点击刷新即可同步真实余票。')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    void loadActivity()
  }, [])

  const bookTicket = async () => {
    if (!currentUser) {
      setAuthMode('login')
      setAuthMessage('购票前请先登录。没有账号请先注册。')
      setAuthOpen(true)
      return
    }
    setIsBooking(true)
    setNotice('')
    try {
      const response = await fetch('/api/v1/ticket-orders', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Auth-Token': currentUser.accessToken },
        body: JSON.stringify({
          activityId: activity.id,
          userId: currentUser.id,
          quantity,
          requestId: crypto.randomUUID(),
        }),
      })
      const result = (await response.json()) as ApiResult<{ orderNo?: string }>
      if (!response.ok || result.code !== 0) throw new Error(result.message || '下单失败')
      setNotice(`购票成功！订单 ${result.data?.orderNo ?? '已创建'}，请在“我的票券”中查看。`)
      await loadActivity()
    } catch (error) {
      setNotice(error instanceof Error ? error.message : '下单未完成，请稍后重试。')
    } finally {
      setIsBooking(false)
    }
  }

  const submitAuth = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const username = authForm.username.trim()
    const password = authForm.password
    if (username.length < 6 || username.length > 19 || password.length < 6 || password.length > 19) {
      setAuthMessage('账号和密码都必须为 6–19 个字符。')
      return
    }
    setAuthMessage('')
    try {
      const response = await fetch(`/api/v1/auth/${authMode === 'login' ? 'login' : 'register'}`, {
        method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ username, password }),
        signal: AbortSignal.timeout(10000),
      })
      const result = (await response.json()) as ApiResult<LoginUser>
      if (!response.ok || result.code !== 0) throw new Error(result.message || '操作失败')
      if (authMode === 'register') {
        setAuthMode('login')
        setAuthForm({ username, password: '' })
        setAuthMessage('注册成功，请使用刚创建的账号登录。')
        return
      }
      setCurrentUser(result.data)
      localStorage.setItem('ticket-platform-user', JSON.stringify(result.data))
      setAuthOpen(false)
      setAuthForm({ username: '', password: '' })
      setNotice(`欢迎回来，${result.data.username}！现在可以购买门票。`)
    } catch (error) {
      setAuthMessage(error instanceof DOMException && error.name === 'TimeoutError' ? '后端未响应，请确认 Spring Boot 已成功启动。' : error instanceof Error ? error.message : '操作失败，请稍后重试。')
    }
  }

  const logout = () => {
    localStorage.removeItem('ticket-platform-user')
    setCurrentUser(null)
    setNotice('已退出登录。再次购票前需要重新登录。')
  }

  return (
    <main>
      <nav className="nav wrap" aria-label="主导航">
        <a className="brand" href="#top" aria-label="Campus Pass 首页"><span>CP</span> Campus Pass</a>
        <div className="nav-links"><a href="#event">精选活动</a><a href="#how">购票说明</a></div>
        {currentUser ? <button className="nav-account" onClick={logout} title="点击退出登录"><UserRound size={15} /> {currentUser.username} · 退出</button> :
          <button className="nav-cta" onClick={() => { setAuthMode('login'); setAuthMessage(''); setAuthOpen(true) }}><LogIn size={15} /> 登录 / 注册</button>}
      </nav>

      <section id="top" className="hero wrap">
        <div className="hero-copy">
          <p className="eyebrow"><Sparkles size={15} /> CAMPUS PASS PRESENTS</p>
          <h1>把热爱，<em>留在现场。</em></h1>
          <p className="hero-text">一张票，奔赴一场属于校园的盛大相遇。校园文化节开幕演出，邀你共赴今秋的第一场欢呼。</p>
          <div className="hero-actions"><a className="primary-button" href="#ticket">立即选票 <ArrowRight size={18} /></a><a className="text-button" href="#event">查看活动信息</a></div>
          <div className="trust-row"><span><ShieldCheck size={17} /> 实名安全购票</span><span><Ticket size={17} /> 即时余票同步</span></div>
        </div>
        <div className="hero-visual" aria-label="校园文化节舞台插画">
          <div className="hero-glow glow-one" /><div className="hero-glow glow-two" />
          <div className="stage"><div className="stage-headline">CULTURE<br /><strong>FEST</strong><br />2026</div><div className="stage-lines" /><div className="crowd" /></div>
          <div className="floating-tag tag-date"><CalendarDays size={16} /><span>9.15<br /><b>周二</b></span></div>
          <div className="floating-tag tag-live"><i /> LIVE NOW</div>
        </div>
      </section>

      <section id="event" className="event-section wrap">
        <div className="section-heading"><p className="eyebrow">FEATURED EVENT</p><h2>今夜，校园不设静音。</h2></div>
        <article className="event-card">
          <div className="event-poster"><div className="poster-orbit" /><span>OPENING<br />SHOW</span><small>2026</small></div>
          <div className="event-body">
            <div className="card-top"><span className="sale-pill">{activity.status === 'ON_SALE' ? '正在开售' : activity.status}</span>{isDemo && <span className="demo-pill">演示数据</span>}</div>
            <h3>{activity.name}</h3>
            <div className="event-meta"><span><CalendarDays size={18} /> {toCnDate(activity.startAt)}</span><span><Clock3 size={18} /> {toTime(activity.startAt)} — {toTime(activity.endAt)}</span><span><MapPin size={18} /> 南校区大学生活动中心礼堂</span></div>
            <p>音乐、舞蹈与光影在此交汇。这里是献给每一位热爱生活的同学的开场白。</p>
            <button className="refresh-button" onClick={() => void loadActivity()} disabled={isLoading}>{isLoading ? <LoaderCircle className="spin" size={16} /> : <RefreshCw size={16} />} 刷新实时余票</button>
          </div>
        </article>
      </section>

      <section id="ticket" className="purchase-section wrap">
        <div className="purchase-copy"><p className="eyebrow">GET YOUR PASS</p><h2>把位置留给<br /><em>此刻的你。</em></h2><p>下单后系统将进行库存锁定，避免超卖。订单将在有效时间内等待支付确认。</p><div className="mini-stats"><div><b>100%</b><span>库存同步</span></div><div><b>24/7</b><span>服务可用</span></div><div><b>1s</b><span>极速锁票</span></div></div></div>
        <div className="ticket-panel">
          <div className="ticket-panel-head"><div><span className="panel-kicker">电子入场券</span><h3>开幕演出 · 普通票</h3></div><span className="price">¥ <b>0</b></span></div>
          <div className="stock-block"><div className="stock-label"><span><Users size={16} /> 还剩 <b>{activity.availableStock.toLocaleString()}</b> 张</span><span>已售 {soldRate}%</span></div><div className="progress"><i style={{ width: `${Math.max(soldRate, 2)}%` }} /></div></div>
          <label className="field-label">当前购票账号<input value={currentUser ? `${currentUser.username}（ID: ${currentUser.id}）` : '请先登录后购票'} disabled /></label>
          <div className="quantity-row"><span>购票数量</span><div className="stepper"><button onClick={() => setQuantity((value) => Math.max(1, value - 1))} aria-label="减少数量"><Minus size={16} /></button><b>{quantity}</b><button onClick={() => setQuantity((value) => Math.min(5, value + 1))} aria-label="增加数量"><Plus size={16} /></button></div></div>
          <button className="buy-button" onClick={() => void bookTicket()} disabled={isBooking || activity.availableStock < quantity}>{isBooking ? <><LoaderCircle className="spin" size={18} /> 正在锁定座位</> : <>确认购票 <ArrowRight size={18} /></>}</button>
          <p className="secure-note"><ShieldCheck size={15} /> 订单受幂等保护，请勿重复提交</p>
          {notice && <div className={`notice ${notice.includes('成功') ? 'notice-success' : ''}`}>{notice.includes('成功') && <CheckCircle2 size={18} />}{notice}</div>}
        </div>
      </section>

      <section id="how" className="how-section wrap"><div><p className="eyebrow">HOW IT WORKS</p><h2>简单三步，<br />奔赴现场。</h2></div><div className="steps"><div><b>01</b><h3>选择活动</h3><p>实时查看演出时间和剩余门票。</p></div><div><b>02</b><h3>安全锁票</h3><p>系统以原子扣减保护每一张票。</p></div><div><b>03</b><h3>领取票券</h3><p>支付确认后，在我的票券查看。</p></div></div></section>

      <footer className="footer wrap"><a className="brand" href="#top"><span>CP</span> Campus Pass</a><p>校园文化节票务平台 · 以可靠技术承接每一次热爱</p><span>© 2026</span></footer>
      {authOpen && <div className="auth-overlay" role="dialog" aria-modal="true" aria-labelledby="auth-title">
        <form className="auth-modal" onSubmit={submitAuth} noValidate>
          <button className="modal-close" type="button" onClick={() => setAuthOpen(false)} aria-label="关闭"><X size={19} /></button>
          <p className="eyebrow">CAMPUS PASS ACCOUNT</p>
          <h2 id="auth-title">{authMode === 'login' ? '欢迎回来' : '创建账号'}</h2>
          <p className="auth-intro">{authMode === 'login' ? '登录后即可安全锁定你的演出门票。' : '注册完成后，请使用该账号登录。'}</p>
          <label>账号<input value={authForm.username} onChange={(event) => setAuthForm({ ...authForm, username: event.target.value })} minLength={6} maxLength={19} required placeholder="6–19 个字符" autoComplete="username" /></label>
          <label>密码<input type="password" value={authForm.password} onChange={(event) => setAuthForm({ ...authForm, password: event.target.value })} minLength={6} maxLength={19} required placeholder="6–19 个字符" autoComplete={authMode === 'login' ? 'current-password' : 'new-password'} /></label>
          {authMessage && <p className="auth-message">{authMessage}</p>}
          <button className="buy-button" type="submit">{authMode === 'login' ? '登录并继续' : '注册账号'} <ArrowRight size={18} /></button>
          <button className="auth-switch" type="button" onClick={() => { setAuthMode(authMode === 'login' ? 'register' : 'login'); setAuthMessage('') }}>{authMode === 'login' ? '没有账号？先注册' : '已有账号？去登录'}</button>
          <small>账号与密码均限制为 6–19 个字符。</small>
        </form>
      </div>}
    </main>
  )
}
