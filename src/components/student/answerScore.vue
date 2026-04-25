<template>
    <div class="score">
        <br />
        <div class="total">
            <div class="look">
                <i class="el-icon-a-061" style="font-size: 32px;"> 本次考试成绩 </i>
            </div>

            <!-- 待批阅状态 -->
            <div v-if="hasSubjective" class="pending-box">
                <div class="pending-icon">
                    <i class="el-icon-time"></i>
                </div>
                <h3 class="pending-title">等待教师批阅主观题</h3>
                <p class="pending-desc">客观题已自动判题，主观题将由教师批阅打分。</p>
                <p class="pending-desc">批阅完成后可在「我的分数」中查看最终成绩。</p>
                <el-tag type="warning" size="medium" style="margin-top:16px;">待批阅</el-tag>
            </div>

            <!-- 正常分数 -->
            <div v-else class="show">
                <div class="number" :class="{ border: isTransition }">
                    <span>{{ score }}</span>
                    <span>分数</span>
                </div>
            </div>

            <ul class="time">
                <li class="start">
                    <span>开始时间</span> <span>{{ startTime }}</span>
                </li>
                <li class="end">
                    <span>结束时间</span> <span>{{ endTime }}</span>
                </li>
            </ul>
        </div>
    </div>
</template>

<script>
export default {
    data() {
        return {
            isTransition: false,
            score: 0,
            startTime: null,
            endTime: null,
            hasSubjective: false,
        };
    },
    created() {
        this.transiton();
        this.getScore();
    },
    methods: {
        transiton() {
            //一秒后过渡
            setTimeout(() => {
                this.isTransition = true;
            }, 1000);
        },
        getScore() {
            let score = this.$route.query.score;
            let startTime = this.$route.query.startTime;
            let endTime = this.$route.query.endTime;
            this.hasSubjective = this.$route.query.hasSubjective === 'true';
            this.score = score;
            this.startTime = startTime;
            this.endTime = endTime;
        },
    },
};
</script>

<style lang="less" scoped>
/* 待批阅状态样式 */
.pending-box {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 40px 20px;
    text-align: center;
}
.pending-icon {
    font-size: 64px;
    color: #E6A23C;
    margin-bottom: 16px;
    animation: pendingPulse 2s ease-in-out infinite;
}
@keyframes pendingPulse {
    0%, 100% { opacity: 1; transform: scale(1); }
    50% { opacity: 0.7; transform: scale(1.05); }
}
.pending-title {
    font-size: 22px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 12px 0;
}
.pending-desc {
    font-size: 14px;
    color: #909399;
    margin: 4px 0;
    line-height: 1.6;
}

.show {
    display: flex;
    justify-content: center;
    align-items: center;
    img {
        width: 160px;
        height: 160px;
    }
    .img1Transform {
        opacity: 1 !important;
        transform: translateX(30px) !important;
        transition: all 0.6s ease !important;
    }
    .img2Transform {
        opacity: 1 !important;
        transform: translateX(-30px) !important;
        transition: all 0.6s ease !important;
    }
    .img1 {
        margin-top: 70px;
        opacity: 0;
        transform: translateX(0px);
        transition: all 0.6s ease;
    }
    .img2 {
        margin-top: 30px;
        opacity: 0;
        transform: translateX(0px);
        transition: all 0.6s ease;
    }
}
.time {
    padding: 0px 70px;
    li {
        display: flex;
        justify-content: space-around;
        padding: 10px;
        margin: 20px 0px;
    }
    li:nth-child(1) {
        background-color: #fcf8e3;
    }
    li:nth-child(2) {
        background-color: #e9f5e9;
    }
}
.border {
    border: 6px solid #36aafd !important;
    transition: all 2s ease;
    width: 160px !important;
    height: 160px !important;
    transform: rotate(360deg) !important;
    opacity: 1 !important;
}
.score {
    max-width: 800px;
    margin: 0 auto;
    .title {
        margin: 60px 0px 30px 0px;
        display: flex;
        justify-content: center;
        align-items: center;
        flex-direction: column;
        .name {
            font-size: 26px;
            color: inherit;
            font-weight: 500;
        }
        .description {
            font-size: 14px;
            color: #888;
        }
    }
    .total {
        border: 1px solid #dbdbdb;
        background-color: #fff;
        padding: 40px;
        .look {
            border-bottom: 1px solid #dbdbdb;
            padding: 0px 0px 14px 14px;
            color: #36aafd;
        }
        .number {
            opacity: 0;
            border: 6px solid #fff;
            transform: rotate(0deg);
            display: flex;
            justify-content: center;
            align-items: center;
            flex-direction: column;
            margin: 0 auto;
            width: 160px;
            height: 160px;
            border-radius: 50%;
            margin-top: 80px;
            margin-bottom: 20px;
            transition: all 1s ease;

            span:nth-child(1) {
                font-size: 36px;
                font-weight: 600;
            }
            span:nth-child(2) {
                font-size: 14px;
            }
        }
    }
}
</style>

