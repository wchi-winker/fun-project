# 针对数据库进行封装成常用工具类
import pymysql

# 针对数据库进行封装成常用工具类
class Storage:

    def __init__(self):
        # 【1】构建数据库连接对象
        self.db = pymysql.connect(host='175.178.244.39', port=3310, user='root', passwd='123456', db='jobdb')

    # 针对于增删改SQL处理
    def execute(self, sql):
        # 【2】获取数据库操作的游标对象
        cursor = self.db.cursor()

        try:
            # 【4】执行SQL语句
            cursor.execute(sql)

            # 【5】提交内容
            self.db.commit()
        except Exception as e:
            print('出现异常', e)

    # 关闭数据库连接
    def disconnect(self):
        self.db.close()

    # 查询操作
    def query(self, sql):
        # 【2】获取数据库操作的游标对象
        cursor = self.db.cursor()

        try:
            # 【4】执行SQL语句
            cursor.execute(sql)
            rows = cursor.fetchall()
            return rows
        except Exception as e:
            print('出现异常', e)


class JobStorage(Storage):

    def insert(self, job):

        sql = f"INSERT INTO `jobdb`.`tb_jobbase`(`jobname`, `exp`, `degree`, `salary`, `company`, `hit`, `city`, `updatetime`) " \
              f"VALUES ('{job.jobname}', '{job.exp}', '{job.degree}', '{job.salary}', '{job.company}', '{job.hit}', '{job.city}','{job.updatetime}')"
        print(sql)
        self.execute(sql)
        print('插入完成')

    def query_all(self):
        sql = "select * from tb_jobbase"
        rows = self.query(sql)
        for row in rows:
            print(row)
            print('岗位名称：',row[1])
