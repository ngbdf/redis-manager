import axios from 'axios';//引入axios

export function fetch(target, options){
    return new Promise((resolve, reject) => {
        const instance = axios.create({  //instance创建一个axios实例，可以自定义配置，可在 axios文档中查看详情
            //所有的请求都会带上这些配置，比如全局都要用的身份信息等。
            headers: {
                'Content-Type': 'application/json',
                // 'token_in_header': global_.token,//token从全局变量那里传过来
            },
            timeout:30 * 1000 // 30秒超时
        });
        instance(options)
            .then(response => { //then 请求成功之后进行什么操作

                if (response.data.code === 201 && target.$route.name !== '/login') {
                    target.$message.warning('登录过期,请重新登录');
                    target.$router.push('/login');
                    return;
                }

                if (response.data.code === 202 && target.$route.name !== '/login') {
                    target.$message.warning('您的账户在别处登录');
                    target.$router.push('/login');
                    return;
                }

                resolve(response);//把请求到的数据发到引用请求的地方
            })
            .catch(error => {
                console.log('请求异常信息：'+error);
                // target.$message.warning('网络错误');
                reject(error);
            });
    });
}