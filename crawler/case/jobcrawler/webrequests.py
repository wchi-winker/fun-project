import requests


# 获取网页数据文本信息
def get_data(url, encoding='utf8'):
    headers = {'User-Agent': get_headers()}
    response = requests.get(url, headers=headers)
    response.encoding = encoding
    data = response.text
    return data


# 获取headers
def get_headers():
    return 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36'
