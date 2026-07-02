import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

export function formatDate(date, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!date) return ''
  return dayjs(date).format(format)
}

export function formatRelativeTime(date) {
  if (!date) return ''
  return dayjs(date).fromNow()
}

export function formatDateSimple(date) {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD')
}
