import time
import random
from bs4 import BeautifulSoup
from case.jobcrawler import webrequests
from case.jobcrawler import jobobj
from case.jobcrawler import storagebase


# 岗位数据爬取类
class JobCrawler:

    # 加载配置文件
    def __load_conf(self):
        citys = []
        jobs = []
        with open('./config/city.conf', encoding='utf-8') as f:
            for city in f:
                citys.append(city.strip())

        with open('./config/jobname.conf', encoding='utf-8') as f:
            for job in f:
                jobs.append(job.strip())

        return citys, jobs

    # 通过城市、岗位、页数定位网页
    def start(self):

        citys, jobs = self.__load_conf()
        
        for city in citys:
            for job in jobs:
                page = 1
                while True:
                    url = f'https://www.jobui.com/jobs?jobKw={job}&cityKw={city}&n={page}'
                    print(f"开始爬取{city}岗位为{job}的第{page}页数据")
                    flag = self.parser_bs(url, city)
                    if flag:
                        page += 1
                        time.sleep(random.randint(1, 2))
                    else:
                        break

    # 针对岗位基本信息进行爬取及解析
    def parser_bs(self, url, city):
        # # 【2】获取URL文本内容
        data = webrequests.get_data(url)
        soup = BeautifulSoup(data, 'html.parser')
        job_lists = soup.find_all('div', attrs={'class': 'c-job-list'})
        if job_lists:
            # 获取基本信息
            self.parser_info(job_lists, city)
            return True
        else:
            return False

    # 针对网页数据进行解析
    def parser_info(self, job_lists, city):
        for item in job_lists:
            job = jobobj.Job()
            job.jobname = item.find('h3').text
            spans = item.find('div', attrs={'class': 'job-desc'}).find_all('span')
            job.exp = spans[0].text
            job.degree = spans[1].text
            job.salary = spans[2].text
            job.company = item.find('a', attrs={'class': 'job-company-name'}).text
            # 点击量信息 coding...
            job.hit = item.find('span', attrs={'class': 'job-desc'}).text.strip()

            # 岗位详情地址
            jobaddr = item.find('a', attrs={'class': 'job-name'}).get('href')
            joburl = f'https://www.jobui.com/{jobaddr}'

            # time.sleep(random.randint(1, 5))
            # 获取详情信息
            self.parser_detail(joburl, job)
            # 数据库存储
            storage = storagebase.JobStorage()
            storage.insert(job)

    # 针对岗位详情页进行数据获取及解析
    def parser_detail(self, url, job):

        try:
            data = webrequests.get_data(url)
            soup = BeautifulSoup(data, 'html.parser')
            # 更新时间
            job.updatetime = soup.find('span', attrs={'class': 'fs16 gray9'}).text.strip()
            # 岗位详情、地址、来源网站  coding.... ------待完成
            
            pass
        except Exception as e:
            print(e)


if __name__ == '__main__':
    jobCrawler = JobCrawler()
    jobCrawler.start()

