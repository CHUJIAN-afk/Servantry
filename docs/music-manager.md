# 音乐管理器

`first.summoner.client.MusicManager` 仅在客户端主线程调用，使用 `Holder<SoundEvent>` 标识音乐。资源需要注册 SoundEvent，且 `sounds.json` 对应条目必须设置 `stream: true`。音量还会乘上玩家设置中的音乐音量与主音量，不衰减距离。

## 播放与音量

```java
var sound = SummonerSoundRegister.DoomsdayStarPhase1;
MusicManager.play(sound);
MusicManager.setVolume(sound, 0.5f);
MusicManager.fadeVolume(sound, 1, 20);
```

`play` 刷新 10 tick 存活时间，需要在音乐应继续存在的每个客户端 tick 调用；重复调用不会重播、改变音量或重置进度。`setVolume` 即时设置 0～1 的音量，`fadeVolume` 在给定 MC tick 数内线性淡化，20 tick 为一秒；不要每 tick 重新发起同一次淡化。开始时设为零音量仍可播放，随后淡入。

```java
MusicManager.stop(sound);
MusicManager.stop(sound, 20);
```

前者立即停播，后者淡出一秒再释放。开始淡出后不再对该曲调用 `play`。停止刷新 `play` 则在存活时间耗尽后立即释放。

## 设置播放进度

```java
MusicManager.setPosition(sound, 30.5);
MusicManager.play(sound);
```

进度为从曲首起的秒数，负值按零处理，超过长度则定位到曲尾。未播放时设置首播位置；播放中会关闭旧实例，在下一个客户端 tick 以新流定位，保留当前音量、未完成的淡化和循环区间。跳转使用 Vorbis 采样定位，不是只修改逻辑计时器，也不依赖只能定位到已排队片段的 OpenAL 偏移。

跳转会异步重建流，不保证跳转过程无间隔；需要音乐间的过渡时，可先对另一首曲设置位置和零音量，再淡入，同时让旧曲淡出。

## 循环区间

```java
MusicManager.setLoop(sound, loopStartSeconds, loopEndSeconds);
MusicManager.play(sound);
```

起止点使用秒数，要求有限数值且 `0 <= start < end`，循环终点截断到曲长。第一次仍从曲首或 `setPosition` 指定的位置播放，抵达终点后回到起点。解码器按采样帧裁切，在同一 PCM 缓冲中继续填充下一轮，不插入静音块、不重启播放通道。是否存在听感上的接缝仍取决于指定循环点与原音频。

建议在播放前设定区间；运行中调整区间会在下一次解码填充时生效，已经排入原版通道的音频不会倒退，最多约四秒。

```java
MusicManager.clearLoop(sound);
```

关闭后继续播放到曲尾。没有循环配置时只播放一次；每 tick 的 `play` 不会在自然结束后自动重播。需要重新开始时调用 `setPosition(sound, 0)`。

## 末世星接入

`AstrageldonMusic` 选择玩家 200 格内最近的存活 Boss，一阶段播放《宙极之恶》，进入 `Phase17` 时切换《天河荒溃》，交叉淡化为 10 tick。离场或玩家死亡淡出，换维度和音频资源重载清理实例；多 Boss 使用同一首曲时不重置进度。

两首曲的 SoundEvent、`stream: true` 和原模组音频均已接入。指导文件未给出明确音乐循环点，所以没有为这两首曲调用 `setLoop`，暂不循环。后续确认起止秒数后在首次 `play` 前配置即可，不必修改解码器或 AI。
