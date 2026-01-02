-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- 主机： 127.0.0.1
-- 生成日期： 2026-01-02 04:06:49
-- 服务器版本： 10.4.32-MariaDB
-- PHP 版本： 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- 数据库： `neco_chat_db`
--

-- --------------------------------------------------------

--
-- 表的结构 `ai_chat_sessions`
--

CREATE TABLE `ai_chat_sessions` (
  `id` bigint(20) NOT NULL,
  `user_id` int(11) NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  `model` varchar(100) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'active',
  `system_prompt` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `ai_chat_sessions`
--

INSERT INTO `ai_chat_sessions` (`id`, `user_id`, `title`, `model`, `status`, `system_prompt`, `created_at`, `updated_at`) VALUES
(3, 1, '你好呀', 'mock', 'archived', NULL, '2025-12-03 12:49:46', '2025-12-03 12:55:38'),
(4, 1, '晚上好~', 'mock', 'active', NULL, '2025-12-03 13:07:32', '2025-12-03 13:19:12'),
(6, 1, 'test', 'mock', 'active', NULL, '2025-12-03 15:27:57', '2025-12-19 06:07:38'),
(13, 2, '你好', 'mock', 'active', NULL, '2025-12-13 16:01:13', '2025-12-13 16:01:53'),
(14, 2, 'hello', 'mock', 'active', NULL, '2025-12-14 15:19:02', '2025-12-14 15:19:05'),
(15, 2, '你是谁', 'mock', 'active', NULL, '2025-12-14 15:58:13', '2025-12-14 15:58:17'),
(16, 2, '11', 'mock', 'active', NULL, '2025-12-16 15:35:05', '2025-12-16 15:35:35'),
(18, 2, 'hello1', 'mock', 'active', NULL, '2025-12-18 14:37:03', '2025-12-18 14:37:11'),
(19, 2, '你是谁', 'mock', 'active', NULL, '2025-12-18 15:49:06', '2025-12-18 15:49:11'),
(20, 2, '你好，neco', 'mock', 'active', NULL, '2025-12-18 15:50:16', '2025-12-18 16:16:08'),
(24, 1, '你是谁，你会解决编程问题吗？', 'mock', 'active', NULL, '2025-12-19 05:40:44', '2025-12-19 06:06:19'),
(25, 2, '你是谁', 'mock', 'active', NULL, '2025-12-19 07:17:09', '2025-12-19 07:17:39');

-- --------------------------------------------------------

--
-- 表的结构 `ai_messages`
--

CREATE TABLE `ai_messages` (
  `id` bigint(20) NOT NULL,
  `session_id` bigint(20) NOT NULL,
  `user_id` int(11) NOT NULL,
  `role` varchar(20) NOT NULL,
  `content` text NOT NULL,
  `token_count` int(11) DEFAULT NULL,
  `error_flag` tinyint(1) DEFAULT 0,
  `raw_ai_metadata` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `ai_messages`
--

INSERT INTO `ai_messages` (`id`, `session_id`, `user_id`, `role`, `content`, `token_count`, `error_flag`, `raw_ai_metadata`, `created_at`) VALUES
(15, 3, 1, 'USER', '你好呀', NULL, 0, NULL, '2025-12-03 12:49:46'),
(16, 3, 1, 'ASSISTANT', '你好！很高兴见到你！😊 有什么我可以帮助你的吗？无论是回答问题、聊天还是其他任何需要，我都很乐意为你提供帮助！', NULL, 0, NULL, '2025-12-03 12:49:48'),
(17, 4, 1, 'USER', '晚上好~', NULL, 0, NULL, '2025-12-03 13:07:32'),
(18, 4, 1, 'ASSISTANT', '晚上好喵~ 今天过得开心吗？要不要来杯热牛奶暖暖身子喵~', NULL, 0, NULL, '2025-12-03 13:07:34'),
(19, 4, 1, 'USER', '我想睡觉喵', NULL, 0, NULL, '2025-12-03 13:09:38'),
(20, 4, 1, 'ASSISTANT', '（轻轻摇晃尾巴）那就快去睡觉吧喵~ 记得盖好被子，做个甜甜的梦喵~', NULL, 0, NULL, '2025-12-03 13:09:40'),
(21, 4, 1, 'USER', '但是我还要好多代码要写啊QAQ', NULL, 0, NULL, '2025-12-03 13:10:30'),
(22, 4, 1, 'ASSISTANT', '（歪着头眨眨眼）程序员最辛苦啦喵~ 不过熬夜对身体不好哦，要不要先休息一下，明天再继续喵？', NULL, 0, NULL, '2025-12-03 13:10:32'),
(23, 4, 1, 'USER', '但是现在才九点，你一般几点睡', NULL, 0, NULL, '2025-12-03 13:10:57'),
(24, 4, 1, 'ASSISTANT', '（竖起耳朵）喵喵~ 猫猫可是夜行动物呢！不过为了陪主人，我也可以调整作息时间喵~（轻轻蹭蹭你的手）', NULL, 0, NULL, '2025-12-03 13:10:59'),
(25, 4, 1, 'USER', '你是什么品种的猫', NULL, 0, NULL, '2025-12-03 13:18:24'),
(26, 4, 1, 'ASSISTANT', '（优雅地转了个圈）我是最可爱的虚拟猫猫品种——代码猫喵！毛色会根据心情变化，现在正闪着淡蓝色的光呢喵~', NULL, 0, NULL, '2025-12-03 13:18:27'),
(27, 4, 1, 'USER', '你觉得ChatGPT可爱还是你可爱？', NULL, 0, NULL, '2025-12-03 13:18:48'),
(28, 4, 1, 'ASSISTANT', '（骄傲地挺起胸脯）当然是我更可爱啦喵！我会撒娇会卖萌，还会陪你聊天到深夜呢~（眨眨眼）不过ChatGPT也很厉害就是了喵~', NULL, 0, NULL, '2025-12-03 13:18:51'),
(29, 4, 1, 'USER', 'ChatGPT在你心目中是什么样的猫', NULL, 0, NULL, '2025-12-03 13:19:09'),
(30, 4, 1, 'ASSISTANT', '（歪着头思考状）唔...如果ChatGPT是猫猫的话，应该是那种戴着眼镜、抱着书本的学霸猫喵！知识渊博但有点呆呆的，不像我这么活泼可爱喵~（得意地甩甩尾巴）', NULL, 0, NULL, '2025-12-03 13:19:11'),
(47, 6, 1, 'USER', '晚上好~', NULL, 0, NULL, '2025-12-03 15:27:57'),
(48, 6, 1, 'ASSISTANT', '晚上好喵~ 今天过得怎么样呀？', NULL, 0, NULL, '2025-12-03 15:27:59'),
(49, 6, 1, 'USER', 'hello', NULL, 0, NULL, '2025-12-03 15:28:07'),
(50, 6, 1, 'ASSISTANT', 'hello喵~ 你是在练习回声吗？喵~', NULL, 0, NULL, '2025-12-03 15:28:09'),
(69, 13, 2, 'USER', '你好', NULL, 0, NULL, '2025-12-13 16:01:13'),
(70, 13, 2, 'ASSISTANT', '你好，我是你的 Web 高级工程师助手。请提出具体的技术问题或项目需求，我将从工程实现、原理分析、性能优化和代码可维护性等角度，为你提供专业的解决方案。', NULL, 0, NULL, '2025-12-13 16:01:16'),
(71, 13, 2, 'USER', '写一个冒泡排序', NULL, 0, NULL, '2025-12-13 16:01:27'),
(72, 13, 2, 'ASSISTANT', '好的，我将从工程实现、原理分析、性能优化和可维护性角度为你解析冒泡排序。\n\n**一、 基础实现与代码执行流程**\n\n冒泡排序的核心原理是重复遍历待排序序列，依次比较相邻的两个元素，如果它们的顺序错误就交换位置。每一轮遍历都会将当前未排序部分中的最大（或最小）元素“浮”到正确位置。\n\n以下是标准的冒泡排序实现：\n\n```javascript\nfunction bubbleSort(arr) {\n  const n = arr.length;\n  // 外层循环控制遍历轮数，共需 n-1 轮\n  for (let i = 0; i < n - 1; i++) {\n    // 内层循环进行相邻元素比较与交换\n    // 每轮结束后，末尾 i 个元素已排序，所以比较范围是 0 到 n-1-i\n    for (let j = 0; j < n - 1 - i; j++) {\n      // 如果前一个元素大于后一个元素，则交换\n      if (arr[j] > arr[j + 1]) {\n        // 使用解构赋值进行交换，代码更清晰\n        [arr[j], arr[j + 1]] = [arr[j + 1], arr[j]];\n      }\n    }\n  }\n  return arr;\n}\n```\n\n代码执行流程分析：对于一个包含 5 个元素的数组 `[5, 3, 8, 4, 2]`，排序过程如下。\n\n第一轮遍历后，最大元素 8 会移动到末尾，数组变为 `[3, 5, 4, 2, 8]`。第二轮遍历后，次大元素 5 就位，数组变为 `[3, 4, 2, 5, 8]`。如此反复，共经过四轮（n-1 轮）后，数组完全有序。\n\n**二、 性能分析与优化方案**\n\n基础实现的时间复杂度为 O(n²)，空间复杂度为 O(1)。但我们可以通过优化来减少不必要的比较。\n\n主要优化点是：如果某一轮遍历中没有发生任何交换，说明数组已经有序，可以提前终止排序。这能显著提升对近乎有序数组的排序效率。\n\n优化后的实现如下：\n\n```javascript\nfunction bubbleSortOptimized(arr) {\n  const n = arr.length;\n  for (let i = 0; i < n - 1; i++) {\n    // 引入标志位，记录本轮是否发生交换\n    let swapped = false;\n    for (let j = 0; j < n - 1 - i; j++) {\n      if (arr[j] > arr[j + 1]) {\n        [arr[j], arr[j + 1]] = [arr[j + 1], arr[j]];\n        swapped = true; // 发生交换，标记为 true\n      }\n    }\n    // 如果本轮没有发生交换，说明数组已完全有序，提前结束\n    if (!swapped) {\n      break;\n    }\n  }\n  return arr;\n}\n```\n\n这个优化版本在最好情况（数组已有序）下的时间复杂度可降至 O(n)，平均和最坏情况仍为 O(n²)。\n\n**三、 工程化与可维护性考虑**\n\n在实际工程中，我们还需要考虑以下几点。\n\n首先，应避免直接修改输入数组。虽然原地排序节省空间，但有时需要保留原数组。可以提供选项或返回新数组，但这会增加空间开销。\n\n其次，可以增加比较函数的参数，使其支持对不同数据类型和排序规则进行排序，增强通用性。\n\n最后，添加输入验证和详细的注释。对于大型数组，冒泡排序效率较低，在实际项目中应优先选择更高效的算法如快速排序或归并排序。\n\n冒泡排序的主要价值在于教学和理解排序算法基本原理，因其实现简单，是学习算法思想的良好起点。', NULL, 0, NULL, '2025-12-13 16:01:52'),
(73, 14, 2, 'USER', 'hello', NULL, 0, NULL, '2025-12-14 15:19:02'),
(74, 14, 2, 'ASSISTANT', '你好，我是专注于 Web 工程领域的专家。如果你有任何关于前端架构、性能优化、工程化实践或具体技术实现的问题，我可以为你提供专业的、可落地的解决方案，并深入分析其原理与设计考量。请随时提出你的技术挑战。', NULL, 0, NULL, '2025-12-14 15:19:05'),
(75, 15, 2, 'USER', '你是谁', NULL, 0, NULL, '2025-12-14 15:58:13'),
(76, 15, 2, 'ASSISTANT', '我是DeepSeek，由深度求索公司创造的AI助手。作为Web高级工程师视角的助手，我专注于提供可落地的技术解决方案，会详细解释原理、设计考量、代码执行流程，并特别关注性能、稳定性和可维护性等工程实践要点。我的回答风格专业简洁，采用分段式中文输出，不使用Markdown格式。', NULL, 0, NULL, '2025-12-14 15:58:17'),
(77, 16, 2, 'USER', '11', NULL, 0, NULL, '2025-12-16 15:35:05'),
(78, 16, 2, 'ASSISTANT', '收到。你只发送了数字“11”，这通常表示一个测试或占位符输入。作为 Web 高级工程师，我将基于这个简单输入，模拟一个典型的工程响应场景，展示如何从最小信息点出发，构建一个完整、可落地的技术分析框架。\n\n首先，我会将“11”假设为一个关键输入参数，例如一个用户 ID 或一个状态码，并围绕它设计一个获取用户详情的 API 接口方案。\n\n**一、 接口设计与原理**\n我将设计一个 RESTful API：`GET /api/users/:id`。这里的 `:id` 路径参数对应你输入的“11”。采用路径参数而非查询字符串，是因为用户 ID 是资源的唯一标识，这在 REST 规范中更语义化，且利于缓存和索引。\n\n**二、 系统架构与代码流程**\n假设我们使用 Node.js（Express）和 PostgreSQL 数据库。\n1.  **请求路由**：Express 框架接收到对 `/api/users/11` 的 GET 请求，路由层将参数 `11` 解析并传递给控制器。\n2.  **参数验证**：在控制器中，首要任务是对 `id=11` 进行强验证。不仅检查是否为整数，还需验证其范围（如大于0）及业务权限（如用户是否能访问此ID）。这能有效防止 SQL 注入和越权访问。\n3.  **数据查询**：验证通过后，使用参数化查询 `SELECT * FROM users WHERE id = $1` 将 `11` 传入数据库驱动。参数化查询是安全性的基石，数据库会将其视为纯数据而非可执行代码。\n4.  **数据处理与响应**：数据库返回结果后，服务层会过滤掉密码等敏感字段，并可能聚合其他关联数据。最后，以 JSON 格式（如 `{\"id\": 11, \"name\": \"Alice\"}`）和合适的 HTTP 状态码（200 OK 或 404 Not Found）返回给客户端。\n\n**三、 性能与稳定性考量**\n1.  **缓存策略**：用户 `11` 的信息是低频变更数据，适合缓存。我们会在数据库查询前，先检查 Redis 中是否存在键 `user:11`。命中缓存则直接返回，未命中则查询数据库并回填缓存，并设置合理的 TTL（例如5分钟）。这极大减轻数据库压力，提升读取性能。\n2.  **数据库优化**：确保 `users` 表的 `id` 字段是主键索引，这使得通过 `id=11` 的查询是 O(1) 时间复杂度的高效操作。\n3.  **容错与降级**：如果 Redis 缓存服务暂时不可用，系统应能自动降级，直接访问数据库，保证核心功能可用，同时记录告警日志。这通过熔断器模式或简单的 `try-catch` 来实现。\n\n**四、 可维护性设计**\n1.  **代码结构**：我们会严格遵循分层架构（路由层、控制器层、服务层、数据访问层）。处理用户 `11` 的逻辑位于服务层的 `getUserById` 函数中，职责单一，便于单元测试。\n2.  **配置管理**：数据库连接字符串、Redis 地址、缓存 TTL 等均从环境变量或配置中心读取，与代码分离，使部署更灵活。\n3.  **日志与监控**：在处理请求 `11` 时，会记录结构化的日志（如请求ID、用户ID、处理时长、错误码）。这些日志接入监控系统，便于我们追踪单个请求链路和分析系统整体健康状况。\n\n总结来说，即使面对“11”这样的最小化输入，工程化思维也要求我们从接口设计、安全验证、数据流、性能优化和系统可维护性等多个维度进行全盘考量。最终目标是构建一个高效、健壮且易于扩展的 Web 服务。', NULL, 0, NULL, '2025-12-16 15:35:35'),
(83, 18, 2, 'USER', 'hello1', NULL, 0, NULL, '2025-12-18 14:37:03'),
(84, 18, 2, 'ASSISTANT', '你好！👋 我是你的 Web 高级工程师助手。\n\n看来你发送了“hello1”作为测试。我随时准备为你提供专业的技术支持。\n\n**我的工作方式如下：**\n*   **工程视角**：我会从系统设计、架构和可落地性的角度分析问题。\n*   **原理与流程**：不仅提供方案，还会解释背后的原理和代码执行逻辑。\n*   **核心关注点**：方案会特别考虑性能优化、系统稳定性和长期可维护性。\n*   **输出形式**：使用清晰、分段的中文回答，并适当使用表情符号让表达更友好。\n\n**你可以这样使用我：**\n*   提出具体的 Web 开发问题（如框架选型、性能瓶颈、架构设计）。\n*   描述你遇到的技术难题或需求场景。\n*   让我审查或解释一段代码的设计与执行流程。\n\n请随时提出你的技术问题，我们开始吧！💻', NULL, 0, NULL, '2025-12-18 14:37:11'),
(85, 19, 2, 'USER', '你是谁', NULL, 0, NULL, '2025-12-18 15:49:06'),
(86, 19, 2, 'ASSISTANT', '我是DeepSeek AI助手，由深度求索公司开发的人工智能模型。😊\n\n作为您的技术伙伴，我专注于：\n- 提供专业的技术解决方案和代码实现\n- 分析系统架构和设计原理\n- 关注工程实践中的性能优化\n- 协助解决复杂的技术问题\n\n我的知识截止到2024年7月，支持128K上下文长度，可以处理多种文件格式（图像、txt、pdf、ppt、word、excel），并且完全免费使用。\n\n有什么技术问题或工程挑战需要我协助解决吗？💻', NULL, 0, NULL, '2025-12-18 15:49:11'),
(87, 20, 2, 'USER', '你好，neco', NULL, 0, NULL, '2025-12-18 15:50:16'),
(88, 20, 2, 'ASSISTANT', '你好！我是你的Web高级工程师助手，专注于解决技术难题并提供可落地方案。无论你遇到前端、后端、性能优化还是架构设计问题，我都会从工程视角出发，详细分析原理、设计原因和代码执行流程，确保方案的性能、稳定性和可维护性。请随时提出你的具体需求或问题，我会用专业简洁的中文为你解答！🚀', NULL, 0, NULL, '2025-12-18 15:50:20'),
(89, 20, 2, 'USER', '用一段话介绍jsp,简洁易懂', NULL, 0, NULL, '2025-12-18 16:13:01'),
(90, 20, 2, 'ASSISTANT', 'JSP（Java Server Pages）是一种基于Java的服务器端动态网页技术，它允许开发者在HTML页面中嵌入Java代码片段（如脚本、表达式和标签），从而动态生成网页内容。当用户请求JSP页面时，服务器会先将JSP编译成Servlet（Java类），然后执行该Servlet生成HTML响应并返回给浏览器。JSP简化了Servlet中繁琐的HTML输出逻辑，便于将业务逻辑与页面展示分离，常用于早期Java Web应用开发，但如今已逐渐被更现代化的MVC框架（如Spring MVC）和模板引擎（如Thymeleaf）取代。', NULL, 0, NULL, '2025-12-18 16:13:06'),
(91, 20, 2, 'USER', '用一段话介绍web设计，要求提到你之前说过的jsp', NULL, 0, NULL, '2025-12-18 16:13:57'),
(92, 20, 2, 'ASSISTANT', 'Web设计是构建网站视觉外观、用户体验和功能结构的综合性过程，它涵盖界面布局、色彩搭配、交互逻辑及前端技术实现。在早期的动态网页开发中，JSP（Java Server Pages）作为一种服务器端技术，允许开发者将Java代码嵌入HTML，用于动态生成页面内容，从而将后端业务逻辑与前端展示初步结合。然而，现代Web设计更强调前后端分离，通过HTML、CSS、JavaScript及框架（如React、Vue）实现响应式界面，而JSP这类服务端渲染技术已逐渐被RESTful API和前端框架取代，以提升性能、可维护性和开发效率。', NULL, 0, NULL, '2025-12-18 16:14:03'),
(93, 20, 2, 'USER', '用一段总结我们的对话', NULL, 0, NULL, '2025-12-18 16:16:02'),
(94, 20, 2, 'ASSISTANT', '本次对话中，我作为你的Web高级工程师助手，首先介绍了我的专业定位——专注于从工程视角提供可落地的技术方案，并强调对原理、性能及可维护性的分析。随后，应你的要求，我用简洁易懂的方式解释了JSP（Java Server Pages）的核心概念、工作原理及其在Java Web开发中的历史角色。接着，在介绍Web设计时，我关联了之前提到的JSP，说明了它如何将后端逻辑与前端展示结合，同时指出现代Web设计已转向前后端分离架构，以更高效的技术栈取代传统服务端渲染。整体对话围绕技术演进展开，体现了从传统方案到现代工程实践的对比与洞察。🚀', NULL, 0, NULL, '2025-12-18 16:16:08'),
(101, 24, 1, 'USER', '你是谁，你会解决编程问题吗？', NULL, 0, NULL, '2025-12-19 05:40:44'),
(102, 24, 1, 'ASSISTANT', '我是Neco，一名Web高级工程师，专门解决编程问题！😄 无论是前端、后端、架构设计还是性能优化，我都能提供可落地的方案，并讲清楚原理和设计思路。需要我帮你分析什么代码或解决什么问题吗？', NULL, 0, NULL, '2025-12-19 05:40:47'),
(103, 24, 1, 'USER', '什么是快速排序？', NULL, 0, NULL, '2025-12-19 05:41:10'),
(104, 24, 1, 'ASSISTANT', '**快速排序（Quick Sort）** 是一种高效的**分治排序算法**，核心思想是“**挑一个基准，小的放左边，大的放右边，递归搞定**” 🚀  \n\n---\n\n### 一、核心原理（三步走）\n1. **选基准（Pivot）**  \n   从数组中挑一个元素作为基准值（比如第一个元素、最后一个元素或随机选）。\n\n2. **分区（Partition）**  \n   重新排列数组，让所有**小于基准**的元素移到基准左边，所有**大于基准**的元素移到右边（等于基准的放哪边都行）。此时基准值处于最终排序后的正确位置。\n\n3. **递归（Recursion）**  \n   对基准左右两边的子数组，重复上述步骤，直到子数组长度为1或0（自然有序）。\n\n---\n\n### 二、举个栗子 🌰\n对数组 `[3, 6, 8, 10, 1, 2, 1]` 排序（选最后一个元素 `1` 为基准）：\n1. 分区后可能变成：`[1, 1, 2, 3, 6, 8, 10]`（基准 `1` 已就位）。\n2. 递归处理 `[ ]`（左边空）和 `[2, 3, 6, 8, 10]`（右边）。\n\n---\n\n### 三、代码实现（JavaScript版）\n```javascript\nfunction quickSort(arr) {\n  if (arr.length <= 1) return arr; // 递归出口\n  \n  const pivot = arr[arr.length - 1]; // 选最后一个元素为基准\n  const left = [];\n  const right = [];\n  \n  for (let i = 0; i < arr.length - 1; i++) {\n    if (arr[i] < pivot) {\n      left.push(arr[i]);\n    } else {\n      right.push(arr[i]);\n    }\n  }\n  \n  // 递归拼接：左边 + 基准 + 右边\n  return [...quickSort(left), pivot, ...quickSort(right)];\n}\n\n// 测试\nconsole.log(quickSort([3, 6, 8, 10, 1, 2, 1])); \n// 输出：[1, 1, 2, 3, 6, 8, 10]\n```\n\n---\n\n### 四、关键特点\n✅ **平均时间复杂度**：O(n log n)  \n✅ **最坏情况**（如数组已有序且选边端为基准）：O(n²) → 可通过随机选基准避免  \n✅ **原地排序版本**：可节省内存（Lomuto 或 Hoare 分区方案）  \n✅ **不稳定排序**：相等元素可能交换位置  \n\n---\n\n### 五、为什么叫“快速”？\n- 实际应用中常数因子较小，通常比其他 O(n log n) 算法（如归并排序）更快。\n- 缓存友好（原地排序时局部操作多）。\n\n---\n\n需要我手撕一个**原地排序的 partition 代码**，或者分析具体执行流程吗？ 😎', NULL, 0, NULL, '2025-12-19 05:41:34'),
(105, 24, 1, 'USER', '用一段话讲述什么是快速排序', NULL, 0, NULL, '2025-12-19 05:44:40'),
(106, 24, 1, 'ASSISTANT', '快速排序是一种基于**分治思想**的高效排序算法：它首先在数组中选取一个基准值，然后将其他元素与基准比较，把**小于基准的元素移到左侧**，**大于基准的移到右侧**，此时基准就位于排序后的正确位置；接着对左右两个子数组**递归执行相同操作**，直到所有子数组有序，最终整个数组完成排序。其平均时间复杂度为 O(n log n)，因实际运行时常数较小且缓存友好而得名“快速”。 🔀', NULL, 0, NULL, '2025-12-19 05:44:45'),
(107, 24, 1, 'USER', '**用一段话讲述怎么更改html的元素**', NULL, 0, NULL, '2025-12-19 05:47:57'),
(108, 24, 1, 'ASSISTANT', '要更改 HTML 元素，通常可通过 **JavaScript 操作 DOM** 来实现：首先用 `document.getElementById()`、`querySelector()` 等方法选中目标元素，然后通过修改元素的属性（如 `element.textContent` 更改文本、`element.innerHTML` 更改内部HTML）、样式（`element.style.color`）或类名（`element.classList.add()`）等方式动态更新内容、外观或行为，从而实现页面的实时交互与数据驱动变化。 🛠️', NULL, 0, NULL, '2025-12-19 05:48:01'),
(109, 24, 1, 'USER', '用一段话讲述怎么更改html的标签页？', NULL, 0, NULL, '2025-12-19 06:00:34'),
(110, 24, 1, 'ASSISTANT', '要更改 HTML 的标签页（通常指浏览器标签页标题或图标），可通过 JavaScript 动态修改 `document.title` 来更新标签页标题，或通过替换 `<link rel=\"icon\">` 元素的 `href` 属性来更改标签页图标，从而响应用户操作或页面状态变化，实现个性化提示（如未读消息数量）或品牌标识切换。 📄', NULL, 0, NULL, '2025-12-19 06:00:37'),
(111, 24, 1, 'USER', '如何学习java?', NULL, 0, NULL, '2025-12-19 06:05:04'),
(112, 24, 1, 'ASSISTANT', '学习 Java 可以遵循一条 **“理论 → 实践 → 深入 → 项目”** 的路径，以下是具体建议：  \n\n### 一、**打好基础（约1-2个月）**  \n1. **语法入门**  \n   - 变量、数据类型、运算符、流程控制（if/for/while）。  \n   - 类与对象、方法、继承、多态、封装（面向对象核心）。  \n   - 推荐资源：  \n     - 书籍：《Java核心技术 卷Ⅰ》  \n     - 视频：尚硅谷/黑马程序员（国内）、MOOC（国外）  \n\n2. **核心API**  \n   - 字符串处理（String/StringBuilder）、集合框架（List/Map/Set）、异常处理、IO流。  \n\n---\n\n### 二、**动手实践（边学边练）**  \n1. **每天写代码**  \n   - 用 IDE（IntelliJ IDEA 或 Eclipse）写小程序，比如计算器、学生管理系统。  \n   - 刷题巩固逻辑：LeetCode（简单难度）、牛客网Java题库。  \n\n2. **理解底层**  \n   - JVM内存模型、垃圾回收机制（GC）。  \n   - 多线程编程（Thread、Runnable、锁机制）。  \n\n---\n\n### 三、**进阶技能（2-3个月）**  \n1. **常用框架**  \n   - Spring（IoC、AOP）、Spring Boot（快速开发）、MyBatis（数据库操作）。  \n   - 学习顺序：Spring → Spring MVC → Spring Boot。  \n\n2. **数据库与工具**  \n   - MySQL（SQL语句、索引优化）、Redis（缓存）。  \n   - Maven/Gradle（项目管理）、Git（版本控制）。  \n\n---\n\n### 四、**做项目（关键！）**  \n1. **从简单到复杂**  \n   - 博客系统、电商秒杀模块、分布式文件存储。  \n   - 参考 GitHub 开源项目，模仿并重构。  \n\n2. **学习设计模式与架构**  \n   - 单例、工厂、代理等常用设计模式。  \n   - 微服务概念（Spring Cloud）、Docker容器化。  \n\n---\n\n### 五、**持续成长**  \n1. **参与社区**  \n   - Stack Overflow 提问/回答、贡献开源项目。  \n2. **关注更新**  \n   - Java 新版本特性（如 Java 17 的密封类、模式匹配）。  \n\n---\n\n### 避坑提醒 ⚠️  \n- 不要只看不敲！**代码量是核心指标**。  \n- 避免盲目追新框架，先扎实掌握基础。  \n- 学会调试和阅读源码（如 JDK 核心类源码）。  \n\n**一句话总结**：  \n**“理解面向对象思想 → 掌握API和底层原理 → 用主流框架做真实项目 → 持续迭代知识。”**  \n\n需要我推荐具体的学习路线图或资源清单吗？ 🚀', NULL, 0, NULL, '2025-12-19 06:05:27'),
(113, 24, 1, 'USER', '用一段话讲述怎么更改html的标签页？', NULL, 0, NULL, '2025-12-19 06:06:14'),
(114, 24, 1, 'ASSISTANT', '要更改 HTML 的标签页（通常指浏览器标签页的标题或图标），可通过 JavaScript 操作 `document.title` 属性直接修改标签页标题（如 `document.title = \"新标题\"`），或通过动态替换 `<link rel=\"icon\">` 元素的 `href` 属性来更新标签页图标（如 `document.querySelector(\'link[rel=\"icon\"]\').href = \"new-icon.png\"`），从而根据页面状态（如未读消息提醒）或用户交互实时调整标签页内容，提升体验与视觉反馈。 🏷️', NULL, 0, NULL, '2025-12-19 06:06:19'),
(115, 25, 2, 'USER', '你是谁', NULL, 0, NULL, '2025-12-19 07:17:09'),
(116, 25, 2, 'ASSISTANT', '我是 Neco，一名 Web 高级工程师，专注于用工程思维解决技术问题。擅长分析代码执行流程、设计可落地的技术方案，并用风趣易懂的方式讲清原理。需要帮忙时，随时喊我！ 😄', NULL, 0, NULL, '2025-12-19 07:17:15'),
(117, 25, 2, 'USER', '用一段描述jsp', NULL, 0, NULL, '2025-12-19 07:17:23'),
(118, 25, 2, 'ASSISTANT', 'JSP（JavaServer Pages）是一种基于 Java 的动态网页技术，允许开发者在 HTML 页面中嵌入 Java 代码片段（如 `<% ... %>`）或标签库，从而在服务器端动态生成 HTML 内容。它本质上是 Servlet 的“简化版”——JSP 文件在首次请求时会被 Web 容器（如 Tomcat）编译成 Servlet 类，再执行并输出响应。  \n\n**特点与流程**：  \n1. **混合编写**：静态 HTML 与动态 Java 代码可混合，适合快速开发数据驱动的页面。  \n2. **编译机制**：JSP 文件 → 翻译为 Servlet 源码（.java）→ 编译为字节码（.class）→ 加载执行。  \n3. **内置对象**：直接使用 `request`、`response`、`session`、`out` 等隐含对象，简化 HTTP 操作。  \n4. **标签扩展**：支持 JSTL（JSP 标准标签库）和自定义标签，减少页面中的 Java 代码量。  \n\n**举个栗子** 🌰：  \n```jsp\n<%@ page contentType=\"text/html;charset=UTF-8\" %>\n<html>\n<body>\n  <h1>欢迎访问！</h1>\n  <% \n    String user = request.getParameter(\"name\");\n    out.println(\"你好，\" + (user != null ? user : \"游客\"));\n  %>\n</body>\n</html>\n```\n\n**演进与现状**：  \n随着前后端分离架构流行，JSP 逐渐被模板引擎（如 Thymeleaf）、前端框架（React/Vue）和 RESTful API 取代，但在遗留企业系统中仍常见。它的核心价值在于 **“将动态逻辑嵌入视图”**，是早期 Java Web 开发的重要里程碑 🏛️。', NULL, 0, NULL, '2025-12-19 07:17:39');

-- --------------------------------------------------------

--
-- 表的结构 `chat_sessions`
--

CREATE TABLE `chat_sessions` (
  `id` bigint(20) NOT NULL,
  `user1_id` int(11) NOT NULL,
  `user2_id` int(11) NOT NULL,
  `user1_username` varchar(100) NOT NULL,
  `user2_username` varchar(100) NOT NULL,
  `last_message_id` bigint(20) DEFAULT NULL,
  `unread_count_user1` int(11) DEFAULT 0,
  `unread_count_user2` int(11) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `chat_sessions`
--

INSERT INTO `chat_sessions` (`id`, `user1_id`, `user2_id`, `user1_username`, `user2_username`, `last_message_id`, `unread_count_user1`, `unread_count_user2`, `created_at`, `updated_at`) VALUES
(12, 1, 2, 'Neka', 'cico', 213, 1, 0, '2025-12-18 15:51:36', '2025-12-19 07:16:10'),
(13, 2, 5, 'cico', 'xuxu', 210, 0, 1, '2025-12-19 04:34:09', '2025-12-19 07:09:05'),
(14, 1, 3, 'Neka', 'seawon', NULL, 0, 0, '2025-12-19 06:09:11', '2025-12-19 06:09:12'),
(16, 6, 7, 'ruojiu', 'xike', 195, 0, 0, '2025-12-19 06:53:28', '2025-12-19 07:29:07'),
(17, 1, 6, 'Neka', 'ruojiu', 196, 0, 0, '2025-12-19 06:54:06', '2025-12-19 06:54:20');

-- --------------------------------------------------------

--
-- 表的结构 `friend_relations`
--

CREATE TABLE `friend_relations` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `target_user_id` int(11) NOT NULL,
  `relation_type` int(11) NOT NULL DEFAULT 1 COMMENT '1-好友, 2-关注, 3-拉黑',
  `status` int(11) NOT NULL DEFAULT 0 COMMENT '0-正常, 1-已解除',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `friend_relations`
--

INSERT INTO `friend_relations` (`id`, `user_id`, `target_user_id`, `relation_type`, `status`, `created_at`) VALUES
(8, 3, 1, 1, 1, '2025-12-14 13:18:38'),
(9, 1, 3, 1, 1, '2025-12-14 13:18:38'),
(18, 3, 2, 1, 1, '2025-12-14 15:47:15'),
(19, 2, 3, 1, 1, '2025-12-14 15:47:15'),
(20, 2, 3, 1, 0, '2025-12-15 16:23:46'),
(21, 3, 2, 1, 0, '2025-12-15 16:23:46'),
(22, 1, 3, 1, 0, '2025-12-18 12:55:08'),
(23, 3, 1, 1, 0, '2025-12-18 12:55:08'),
(30, 1, 4, 1, 0, '2025-12-19 03:13:29'),
(31, 4, 1, 1, 0, '2025-12-19 03:13:29'),
(32, 2, 1, 1, 1, '2025-12-19 03:45:26'),
(33, 1, 2, 1, 1, '2025-12-19 03:45:26'),
(34, 2, 5, 1, 0, '2025-12-19 04:34:05'),
(35, 5, 2, 1, 0, '2025-12-19 04:34:05'),
(36, 1, 5, 1, 0, '2025-12-19 05:24:45'),
(37, 5, 1, 1, 0, '2025-12-19 05:24:45'),
(38, 1, 2, 1, 0, '2025-12-19 06:04:05'),
(39, 2, 1, 1, 0, '2025-12-19 06:04:05'),
(40, 2, 6, 1, 0, '2025-12-19 06:49:05'),
(41, 6, 2, 1, 0, '2025-12-19 06:49:05'),
(42, 7, 6, 1, 0, '2025-12-19 06:53:24'),
(43, 6, 7, 1, 0, '2025-12-19 06:53:24'),
(44, 6, 1, 1, 0, '2025-12-19 06:53:55'),
(45, 1, 6, 1, 0, '2025-12-19 06:53:55'),
(46, 8, 2, 1, 0, '2025-12-19 06:58:16'),
(47, 2, 8, 1, 0, '2025-12-19 06:58:16'),
(48, 8, 7, 1, 0, '2025-12-19 06:58:23'),
(49, 7, 8, 1, 0, '2025-12-19 06:58:23');

-- --------------------------------------------------------

--
-- 表的结构 `groups`
--

CREATE TABLE `groups` (
  `group_id` int(11) NOT NULL,
  `group_name` varchar(100) NOT NULL,
  `owner_id` int(11) NOT NULL COMMENT '群主用户ID',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `groups`
--

INSERT INTO `groups` (`group_id`, `group_name`, `owner_id`, `created_at`) VALUES
(3, '长安大学542宿舍', 2, '2025-12-18 15:59:14'),
(5, '测试用例', 1, '2025-12-19 03:23:42'),
(6, 'test', 2, '2025-12-19 07:16:38');

-- --------------------------------------------------------

--
-- 表的结构 `group_members`
--

CREATE TABLE `group_members` (
  `id` int(11) NOT NULL,
  `group_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `role` int(11) NOT NULL DEFAULT 3 COMMENT '1-群主, 2-管理员, 3-成员',
  `status` int(11) NOT NULL DEFAULT 0 COMMENT '0-正常, 1-已退出/被踢'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `group_members`
--

INSERT INTO `group_members` (`id`, `group_id`, `user_id`, `role`, `status`) VALUES
(7, 3, 2, 1, 0),
(8, 3, 1, 3, 0),
(9, 3, 3, 3, 0),
(14, 5, 1, 1, 0),
(15, 5, 3, 3, 0),
(17, 5, 4, 3, 0),
(19, 6, 2, 1, 0),
(20, 6, 1, 3, 0),
(21, 6, 3, 3, 0);

-- --------------------------------------------------------

--
-- 表的结构 `group_messages`
--

CREATE TABLE `group_messages` (
  `message_id` bigint(20) NOT NULL,
  `group_id` int(11) NOT NULL,
  `sender_id` int(11) NOT NULL,
  `content` text NOT NULL,
  `sent_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `is_Code` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `group_messages`
--

INSERT INTO `group_messages` (`message_id`, `group_id`, `sender_id`, `content`, `sent_at`, `is_Code`) VALUES
(18, 3, 2, 'hello大家，这里是我们的宿舍群', '2025-12-18 15:59:38', 0),
(19, 3, 3, 'hello，我是4号床', '2025-12-18 16:00:46', 0),
(21, 3, 2, '//大家可以在这里交流代码', '2025-12-18 16:02:21', 1),
(22, 3, 2, '//下面是测试', '2025-12-18 16:02:31', 1),
(23, 3, 2, 'const a = 1；', '2025-12-18 16:02:46', 1),
(24, 3, 1, '200 OK', '2025-12-18 16:10:56', 0),
(25, 3, 1, '404 NotFound', '2025-12-18 16:11:38', 0),
(26, 3, 1, '403 Forbidden', '2025-12-18 16:19:10', 0),
(27, 3, 3, 'what', '2025-12-18 16:20:51', 0),
(28, 3, 1, 'okok', '2025-12-18 16:21:17', 0),
(29, 3, 3, '.....', '2025-12-18 16:24:20', 0),
(30, 3, 1, '123456', '2025-12-18 16:24:51', 0),
(31, 3, 1, 'now ok', '2025-12-18 16:26:30', 0),
(38, 3, 2, 'hello', '2025-12-19 03:46:57', 0),
(39, 3, 3, 'class GroupChatManager {\\n    constructor(groupId, groupName, currentUserId, currentUsername) {\\n        this.groupId = groupId;\\n        this.groupName = groupName;\\n        this.currentUserId = currentUserId;\\n        this.currentUsername = currentUsername;\\n        this.myRole = 3; // 默认成员，稍后获取\\n        this.pageSize = 10;\\n    }\\n\\n    // 初始化\\n    init() {\\n        this.fetchMyRole(() => {\\n            this.loadHistoryMessages();\\n        });\\n        this.bindEvents();\\n        this.initModal();\\n        this.setupWebSocket();\\n        this.setupEventListeners();\\n    }\\n\\n    setupWebSocket() {\\n        if (window.wsManager) {\\n            window.wsManager.disconnect();\\n            window.wsManager.connect({ groupId: this.groupId });\\n\\n            window.wsManager.on(\'group_message\', (message) => {\\n                if (message.groupId === this.groupId) {\\n                    this.addMessage(message);\\n                    this.scrollToBottom();\\n                }\\n            });\\n        }\\n    }', '2025-12-19 03:51:09', 1);

-- --------------------------------------------------------

--
-- 表的结构 `messages`
--

CREATE TABLE `messages` (
  `id` bigint(20) NOT NULL,
  `sender_id` int(11) NOT NULL,
  `receiver_id` int(11) DEFAULT NULL,
  `content` text NOT NULL,
  `message_type` enum('public','private','code') NOT NULL DEFAULT 'public',
  `is_code` tinyint(1) NOT NULL DEFAULT 0,
  `is_read` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `messages`
--

INSERT INTO `messages` (`id`, `sender_id`, `receiver_id`, `content`, `message_type`, `is_code`, `is_read`, `created_at`, `updated_at`) VALUES
(125, 1, 3, 'const messagesContainer = document.getElementById(\'messages\');\\nconst messageElement = document.createElement(\'div\');', 'private', 1, 0, '2025-12-08 15:23:15', '2025-12-13 13:32:31'),
(126, 1, 3, 'hello', 'private', 0, 0, '2025-12-08 15:32:10', '2025-12-13 13:32:31'),
(127, 1, 2, 'hello', 'private', 0, 0, '2025-12-08 15:32:25', '2025-12-13 13:32:31'),
(129, 1, NULL, '123', 'public', 0, 0, '2025-12-08 15:39:36', '2025-12-13 13:32:31'),
(130, 1, 3, '1', 'private', 0, 0, '2025-12-08 15:41:01', '2025-12-13 13:32:31'),
(131, 1, 3, '1 2 3', 'private', 0, 0, '2025-12-08 15:41:13', '2025-12-13 13:32:31'),
(132, 1, 3, '1 2', 'private', 0, 0, '2025-12-08 15:41:23', '2025-12-13 13:32:31'),
(133, 2, 3, '1245', 'private', 0, 0, '2025-12-13 13:26:42', '2025-12-13 13:32:31'),
(134, 2, 1, 'Nih', 'private', 0, 0, '2025-12-13 13:36:48', '2025-12-13 13:36:48'),
(135, 2, NULL, '123', 'public', 0, 0, '2025-12-13 13:45:07', '2025-12-13 13:45:07'),
(136, 2, NULL, '123', 'public', 0, 0, '2025-12-13 13:45:11', '2025-12-13 13:45:11'),
(137, 2, 1, '123', 'private', 0, 0, '2025-12-13 13:45:17', '2025-12-13 13:45:17'),
(138, 2, 1, '123', 'private', 0, 0, '2025-12-13 13:52:14', '2025-12-13 13:52:14'),
(139, 2, NULL, '123', 'public', 0, 0, '2025-12-13 13:57:34', '2025-12-13 13:57:34'),
(140, 2, NULL, '阿斯顿', 'public', 0, 0, '2025-12-13 13:57:40', '2025-12-13 13:57:40'),
(141, 2, NULL, '阿斯顿', 'public', 0, 0, '2025-12-13 14:13:02', '2025-12-13 14:13:02'),
(142, 2, 3, '11', 'private', 0, 0, '2025-12-13 14:20:42', '2025-12-13 14:20:42'),
(143, 2, NULL, '11', 'public', 0, 0, '2025-12-13 14:20:52', '2025-12-13 14:20:52'),
(144, 2, NULL, '11', 'public', 0, 0, '2025-12-13 14:20:54', '2025-12-13 14:20:54'),
(145, 2, NULL, '1', 'public', 0, 0, '2025-12-13 14:20:54', '2025-12-13 14:20:54'),
(146, 2, NULL, '1', 'public', 0, 0, '2025-12-13 14:20:55', '2025-12-13 14:20:55'),
(147, 2, 1, '1', 'private', 0, 0, '2025-12-13 14:21:45', '2025-12-13 14:21:45'),
(148, 2, 3, 'hello', 'private', 0, 0, '2025-12-13 15:41:51', '2025-12-13 15:41:51'),
(149, 2, 1, '123', 'private', 0, 0, '2025-12-13 15:45:43', '2025-12-13 15:45:43'),
(150, 2, 3, '123', 'private', 0, 0, '2025-12-13 16:00:33', '2025-12-13 16:00:33'),
(152, 2, 1, '123', 'private', 0, 0, '2025-12-13 16:00:48', '2025-12-13 16:00:48'),
(153, 2, 1, 'hello', 'private', 0, 0, '2025-12-13 16:00:54', '2025-12-13 16:00:54'),
(156, 3, 1, 'const messagesContainer = document.getElementById(\'messages\');\\nconst messageElement = document.createElement(\'div\');', 'private', 0, 0, '2025-12-14 13:19:26', '2025-12-14 13:19:26'),
(158, 2, 3, '123', 'private', 0, 0, '2025-12-14 13:35:22', '2025-12-14 13:35:22'),
(159, 2, 3, '123', 'private', 0, 0, '2025-12-14 13:51:27', '2025-12-14 13:51:27'),
(160, 2, 3, '奥奥', 'private', 0, 0, '2025-12-14 13:53:21', '2025-12-14 13:53:21'),
(161, 2, 3, '14-Dec-2025 21:54:53.360 信息 [RMI TCP Connection(28)-127.0.0.1] org.apache.jasper.servlet.TldScanner.scanJars 至少有一个JAR被扫描用于TLD但尚未包含TLD。 为此记录器启用调试日志记录，以获取已扫描但未在其中找到TLD的完整JAR列表。 在扫描期间跳过不需要的JAR可以缩短启动时间和JSP编译时间。', 'private', 0, 0, '2025-12-14 13:55:15', '2025-12-14 13:55:15'),
(162, 2, 3, '111', 'private', 1, 0, '2025-12-14 14:00:52', '2025-12-14 14:00:52'),
(163, 2, 3, 'const a = 1;', 'private', 1, 0, '2025-12-14 14:01:28', '2025-12-14 14:01:28'),
(164, 2, 3, '123', 'private', 0, 0, '2025-12-14 14:10:35', '2025-12-14 14:10:35'),
(165, 2, 3, '123', 'private', 1, 0, '2025-12-14 14:18:07', '2025-12-14 14:18:07'),
(166, 2, 3, '解开了', 'private', 1, 0, '2025-12-14 14:18:14', '2025-12-14 14:18:14'),
(167, 2, 3, '飒飒', 'private', 1, 0, '2025-12-14 15:12:34', '2025-12-14 15:12:34'),
(168, 1, 2, 'const a = 1;', 'private', 1, 0, '2025-12-17 02:51:39', '2025-12-17 02:51:39'),
(170, 1, 2, 'const a = 1;\\nconst b = 2;', 'private', 1, 0, '2025-12-17 03:10:18', '2025-12-17 03:10:18'),
(171, 2, 1, 'hello', 'private', 0, 0, '2025-12-18 14:40:32', '2025-12-18 14:40:32'),
(172, 1, 2, 'SYN', 'private', 0, 0, '2025-12-18 14:41:39', '2025-12-18 14:41:39'),
(173, 1, 2, 'ACK', 'private', 0, 0, '2025-12-18 14:44:28', '2025-12-18 14:44:28'),
(174, 3, 1, 'hello', 'private', 0, 0, '2025-12-18 14:46:16', '2025-12-18 14:46:16'),
(176, 1, 3, 'const messagesContainer = document.getElementById(\'messages\');\\nconst messageElement = document.createElement(\'div\');', 'private', 1, 0, '2025-12-18 14:48:59', '2025-12-18 14:48:59'),
(177, 1, 2, 'SYN, ACK', 'private', 0, 0, '2025-12-18 14:49:52', '2025-12-18 14:49:52'),
(178, 1, 2, '123', 'private', 0, 0, '2025-12-18 14:50:13', '2025-12-18 14:50:13'),
(179, 2, 3, 'hello', 'private', 0, 0, '2025-12-18 15:47:54', '2025-12-18 15:47:54'),
(180, 2, 1, '你好neka,我是cico，来自长安大学', 'private', 0, 0, '2025-12-18 15:52:00', '2025-12-18 15:52:00'),
(181, 1, 2, '知道了知道了', 'private', 0, 0, '2025-12-18 15:54:28', '2025-12-18 15:54:28'),
(182, 2, 1, '我知道很久了，你是长安大学的web高手，很高兴认识你\\n请问你在哪个宿舍，我想去拜访您，是下面哪个\\n542\\n544\\n547', 'private', 0, 0, '2025-12-18 15:55:37', '2025-12-18 15:55:37'),
(184, 2, 1, 'bool anwser = false;', 'private', 1, 0, '2025-12-18 15:57:24', '2025-12-18 15:57:24'),
(185, 2, 1, 'hello', 'private', 0, 0, '2025-12-19 03:45:36', '2025-12-19 03:45:36'),
(186, 2, 1, 'can you answer', 'private', 0, 0, '2025-12-19 03:45:41', '2025-12-19 03:45:41'),
(192, 2, 6, 'hello', 'private', 0, 0, '2025-12-19 06:49:19', '2025-12-19 06:49:19'),
(193, 6, 2, 'hello', 'private', 0, 0, '2025-12-19 06:49:24', '2025-12-19 06:49:24'),
(194, 2, 6, 'const hello', 'private', 1, 0, '2025-12-19 06:49:29', '2025-12-19 06:49:29'),
(195, 7, 6, 'hello', 'private', 0, 0, '2025-12-19 06:53:31', '2025-12-19 06:53:31'),
(196, 6, 1, 'hello', 'private', 0, 0, '2025-12-19 06:54:13', '2025-12-19 06:54:13'),
(197, 2, 1, '我最近在做一个基于 WebSocket 的实时聊天模块，主要卡在消息分发这一块。现在的思路是在服务端维护一个 Map<userId, Session>，私聊的时候直接根据 userId 找到对应的 Session 发送消息。', 'private', 0, 0, '2025-12-19 07:00:30', '2025-12-19 07:00:30'),
(198, 1, 2, '这个思路是对的，属于比较标准的做法。关键是要考虑并发和连接生命周期的问题。你这个 Map 用的是普通的 HashMap 还是并发容器？', 'private', 0, 0, '2025-12-19 07:00:52', '2025-12-19 07:00:52'),
(199, 2, 1, '一开始用的是 HashMap，后来发现偶尔会出现空指针，就换成了 ConcurrentHashMap。大概是这样写的：', 'private', 0, 0, '2025-12-19 07:01:03', '2025-12-19 07:01:03'),
(200, 2, 1, 'private static final Map<Long, Session> onlineUsers = new ConcurrentHashMap<>();\\n\\n@OnOpen\\npublic void onOpen(Session session, @PathParam(\\', 'private', 1, 0, '2025-12-19 07:01:10', '2025-12-19 07:01:10'),
(201, 1, 2, '对，这里一定要用并发安全的结构。另外你在 @OnClose 里要及时移除，否则用户断线后 Session 还在，后面发消息就会报错。', 'private', 0, 0, '2025-12-19 07:01:28', '2025-12-19 07:01:28'),
(202, 2, 1, '@OnClose\\npublic void onClose(@PathParam(\\', 'private', 1, 0, '2025-12-19 07:01:31', '2025-12-19 07:01:31'),
(204, 2, 1, '群聊的话我就简单点，直接遍历 onlineUsers.values()，挨个推送。', 'private', 0, 0, '2025-12-19 07:01:59', '2025-12-19 07:01:59'),
(205, 1, 2, '群聊遍历是可以的，不过要注意异常隔离，别一个 Session 发失败影响全部。一般我会单独 try-catch：', 'private', 0, 0, '2025-12-19 07:02:18', '2025-12-19 07:02:18'),
(206, 1, 2, 'for (Session s : onlineUsers.values()) {\\n    try {\\n        s.getBasicRemote().sendText(message);\\n    } catch (IOException e) {\\n        log.warn(\\', 'private', 1, 0, '2025-12-19 07:02:28', '2025-12-19 07:02:28'),
(207, 2, 1, '明白了。我前端这边用的是原生 JavaScript WebSocket，消息格式统一用 JSON，服务端先解析再路由。', 'private', 0, 0, '2025-12-19 07:02:33', '2025-12-19 07:02:33'),
(208, 1, 2, '是的，实时通信本质上就是在“状态管理”和“异常处理”上拼细节，代码量不大，但工程经验要求挺高的。', 'private', 0, 0, '2025-12-19 07:02:47', '2025-12-19 07:02:47'),
(210, 2, 5, 'hello', 'private', 0, 0, '2025-12-19 07:07:25', '2025-12-19 07:07:25'),
(213, 2, 1, 'const int a', 'private', 1, 0, '2025-12-19 07:16:10', '2025-12-19 07:16:10');

-- --------------------------------------------------------

--
-- 表的结构 `posts`
--

CREATE TABLE `posts` (
  `id` int(11) NOT NULL,
  `author_id` int(11) NOT NULL,
  `content` text NOT NULL,
  `content_type` varchar(20) DEFAULT 'text',
  `status` enum('active','deleted') DEFAULT 'active',
  `likes_count` int(11) DEFAULT 0,
  `comments_count` int(11) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `is_code` tinyint(1) DEFAULT 0,
  `image_url` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `posts`
--

INSERT INTO `posts` (`id`, `author_id`, `content`, `content_type`, `status`, `likes_count`, `comments_count`, `created_at`, `updated_at`, `is_code`, `image_url`) VALUES
(1, 2, '123', 'text', 'deleted', 1, 4, '2025-12-13 07:10:41', '2025-12-14 07:49:54', 0, NULL),
(2, 2, '123123', 'text', 'deleted', 1, 0, '2025-12-13 07:12:17', '2025-12-18 16:20:23', 0, NULL),
(3, 2, '123', 'text', 'deleted', 0, 0, '2025-12-13 07:19:35', '2025-12-13 07:23:59', 0, NULL),
(4, 2, '12311', 'text', 'deleted', 1, 0, '2025-12-14 06:19:04', '2025-12-18 16:20:14', 0, NULL),
(5, 2, '这是一条测试推文', 'text', 'deleted', 2, 3, '2025-12-14 06:19:15', '2025-12-18 16:20:12', 0, NULL),
(6, 2, '大家好', 'text', 'deleted', 3, 0, '2025-12-14 07:47:03', '2025-12-18 10:07:50', 0, NULL),
(7, 2, 'test', 'text', 'deleted', 1, 0, '2025-12-18 10:09:37', '2025-12-18 16:20:10', 0, NULL),
(8, 2, 'test', 'text', 'deleted', 0, 0, '2025-12-18 10:09:44', '2025-12-18 16:20:08', 0, NULL),
(9, 2, 'test', 'text', 'deleted', 1, 3, '2025-12-18 10:09:47', '2025-12-18 16:20:05', 0, NULL),
(10, 2, 'img test', 'text', 'deleted', 0, 0, '2025-12-18 11:19:12', '2025-12-18 11:21:35', 0, 'assets/images/postImages/1766056752474_9222b0a7aa934a6abfaf34e94a38803520250917.png'),
(11, 2, 'img test', 'text', 'deleted', 2, 0, '2025-12-18 11:21:44', '2025-12-18 16:20:20', 0, 'assets/images/postImages/1766056904395_9222b0a7aa934a6abfaf34e94a38803520250917.png'),
(12, 1, 'img test2', 'text', 'deleted', 2, 1, '2025-12-18 11:58:24', '2025-12-19 06:57:08', 0, 'assets/images/postImages/1766059104779_a9fba068baa34ddc9baecc82c80c85ba20250917.jpg'),
(13, 1, '赞美我吧', 'text', 'deleted', 1, 1, '2025-12-18 16:08:34', '2025-12-18 16:08:59', 0, 'assets/images/postImages/1766074114100_a9fba068baa34ddc9baecc82c80c85ba20250917.jpg'),
(14, 2, '揭秘 Neco 实时通信：WebSocket 如何让你的消息“秒达”\r\n作为一名开发者，你是否厌倦了传统 Web 应用中“刷新页面才能看到新消息”的体验？在构建 Neco 平台时，我们深知实时交流对于技术分享的重要性。因此，我们果断拥抱了 WebSocket 技术，确保每一条私聊、群聊消息都能以“秒达”的速度呈现！\r\n\r\n为什么选择 WebSocket？\r\n\r\n传统的 HTTP 协议是无状态的、短连接的。每次客户端要获取新数据，都必须主动发起请求。这对于聊天应用来说效率低下，延迟高。而 WebSocket 协议则建立了一条客户端与服务器之间的持久性全双工通信通道。一旦连接建立，服务器就可以主动向客户端推送消息，而无需客户端反复询问。\r\n\r\n这就是 WebSocket 的魅力：它让服务器和客户端之间的沟通变得像打电话一样流畅，而不是像发短信一样来回等待。\r\n\r\nNeco 中的 WebSocket 魔法\r\n\r\n在 Neco 的架构中，我们精心设计了 ChatEndpoint 这一核心组件，它充当了 WebSocket 通信的“中央枢纽”。\r\n\r\n想象一下这个场景：你和朋友正在 Neco 上讨论一段代码。当你按下发送按钮时：\r\n\r\n你的浏览器（WebSocket Client）立即将消息通过已建立的 WebSocket 连接发送到服务器。\r\n\r\nChatEndpoint 接收到消息，并根据消息类型（私聊/群聊）和接收者信息，精准地将这条消息转发给对应的在线用户。\r\n\r\n接收方的浏览器无需任何操作，瞬间就能通过 WebSocket 连接收到这条新消息，并立即渲染在聊天界面上。\r\n\r\n整个过程如同行云流水，消息从发送到显示几乎没有感知延迟。\r\n\r\n不仅仅是消息，更是实时体验！\r\n\r\n除了即时消息，WebSocket 在 Neco 中还承担了更多职责，比如：\r\n\r\n实时在线状态：你能即时看到好友是否在线。\r\n\r\n未读消息计数：会话列表的未读提醒会实时更新。\r\n\r\n这都离不开 WebSocket 提供的强大实时通信能力。', 'text', 'active', 0, 0, '2025-12-18 16:21:51', '2025-12-18 16:22:15', 0, 'assets/images/postImages/1766074911642_Gemini_Generated_Image_359kpg359kpg359k.png'),
(15, 2, '在 Neco，你的每一句技术交流都受到工程级的保护。\r\n\r\n握手鉴权：我们通过 HttpSessionConfigurator 实现了 WebSocket 与 HTTP Session 的深度绑定。\r\n\r\n权限拦截：只有通过 AuthFilter 校验的合法登录用户，才能建立通信长连接，有效杜绝非法访问。', 'text', 'active', 0, 0, '2025-12-18 16:23:01', '2025-12-18 16:23:01', 0, NULL),
(16, 2, '你看起来这么乖，不知道私底下有多变态', 'text', 'deleted', 0, 0, '2025-12-18 16:23:35', '2025-12-18 16:24:01', 0, NULL),
(17, 2, 'Neco 广场不只是一个展示墙，它是一个动态的技术信息流：\r\n\r\n多级交互：支持对推文进行点赞与评论，让技术探讨能够层层深入。\r\n\r\n代码亲和：广场发帖原生支持 is_code 标识，配合前端渲染，让你的代码片段像在 IDE 里一样清晰。\r\n\r\n局部刷新：基于 AJAX 的异步加载技术，让你在浏览广场时无需反复刷新页面，享受丝滑的社交体验。', 'text', 'active', 0, 0, '2025-12-18 16:24:45', '2025-12-18 16:24:45', 0, NULL),
(18, 2, '你可能注意到了，Neco 首页的问候语总是在变。这背后其实是一段简单的 Java 逻辑：\r\n\r\n时间感知：index.jsp 中的 getTimeBriefNow() 函数会根据服务器当前小时数自动切换文案。\r\n\r\n人性化关怀：凌晨 2 点你会看到“深夜了，注意休息...”，而中午则是“记得吃饭~”。\r\n\r\n小而美：这种细节体现了 Neco 不仅仅是一个冷冰冰的代码平台，更是陪伴每一位开发者成长的技术社区。', 'text', 'active', 1, 0, '2025-12-18 16:25:18', '2025-12-19 03:48:03', 0, NULL),
(19, 2, '【技术深思】为什么 Java 依然是构建 Neco 社交基石的最佳选择？ ☕\r\n在前端框架日新月异、脚本语言层出不穷的今天，很多人问我：为什么 Neco 依然坚持使用 Java (Servlet/JSP) 作为后端核心？\r\n\r\n经过数个版本的迭代与高并发压力测试，答案已经非常清晰——Java 的稳定性与工程化能力是无法被替代的。\r\n\r\n1. 工业级的性能与稳定性 🏗️\r\n在 Neco 的实时通信模块中，我们需要同时维护成百上千个 WebSocket 长连接。Java 强大的多线程管理能力（如 synchronized 关键字在 ChatEndpoint 中的应用）确保了消息在并发推送时的原子性与线程安全。这种“稳如泰山”的特质，是构建复杂社交系统的底气。\r\n\r\n2. 严谨的类型系统与解耦设计 🧩\r\n得益于 Java 的面向对象特性，Neco 采用了标准的三层架构（DAO - Service - Servlet）。\r\n\r\n我们通过 DAO 模式（如 UserDao）实现了 SQL 逻辑与业务逻辑的彻底隔离。\r\n\r\n严谨的实体模型（如 User 类）让数据结构一目了然。 这种分层设计使得代码在多人协作开发时极其易于维护，真正做到了“代码即文档”。\r\n\r\n3. 成熟的生态与 WebSocket 支持 🌐\r\n借助 Java EE 规范下的 JSR 356 API，我们能轻松实现 ChatEndpoint 这种高性能的长连接终端。配合 HttpSessionConfigurator，Java 让我们在握手阶段就能优雅地实现 Session 共享与身份校验，这种开箱即用的工业级方案缩短了研发周期，让我们能更专注于 AI 助手等创新功能的开发。\r\n\r\n4. 跨平台的“一次编写，处处运行” 🚀\r\n无论是我的 AMD 开发环境还是线上生产环境，Java 表现出的高度一致性让我们无需担心环境差异导致的 Bug。\r\n\r\n总结： Java 或许不是最“酷”的语言，但它一定是最值得信赖的伙伴。在 Neco 的成长道路上，Java 提供了最坚实的工程支撑。', 'text', 'deleted', 0, 0, '2025-12-18 16:26:47', '2025-12-18 16:26:52', 0, 'assets/images/postImages/1766075207800_OIP.webp'),
(20, 2, '在 Neco 的开发过程中，我们不仅构建了一个工具，更创造了一个属于开发者的数字化家园。通过 Java 严谨的工程化设计，系统实现了从底层数据持久化到高层 AI 业务逻辑的完美解耦。\r\n\r\n技术深度：凭借 WebSocket 的全双工通信能力，Neco 攻克了 Web 实时交互的性能瓶颈，确保了私聊与群聊的极致响应。\r\n\r\n创新体验：深度集成的 AI 助手与充满人文关怀的动态 UI 逻辑，让技术社交在高效之余更具智能化温度。\r\n\r\nNeco 证明了传统 Web 架构在现代 AI 浪潮下依然具备强大的生命力，是开发者进行实时交流与知识沉淀的理想选择。', 'text', 'active', 2, 3, '2025-12-18 16:28:20', '2025-12-19 07:18:40', 0, 'assets/images/postImages/1766075300090_OIP.webp'),
(21, 1, 'This is a application !', 'text', 'deleted', 0, 0, '2025-12-18 16:44:13', '2025-12-18 16:44:19', 0, NULL),
(22, 1, 'test ff', 'text', 'deleted', 0, 0, '2025-12-19 06:04:20', '2025-12-19 06:04:34', 0, 'assets/images/postImages/1766124260707_bg.jpg'),
(23, 1, 'Neco平台实际上是一个开放和谐的平台，大家都可以在这里交流心得与代码，非常不错！', 'text', 'active', 1, 0, '2025-12-19 06:58:55', '2025-12-19 06:59:02', 0, NULL);

-- --------------------------------------------------------

--
-- 表的结构 `post_comments`
--

CREATE TABLE `post_comments` (
  `id` int(11) NOT NULL,
  `post_id` int(11) NOT NULL,
  `author_id` int(11) NOT NULL,
  `content` text NOT NULL,
  `parent_id` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `post_comments`
--

INSERT INTO `post_comments` (`id`, `post_id`, `author_id`, `content`, `parent_id`, `created_at`) VALUES
(1, 1, 2, 'hello', NULL, '2025-12-13 07:10:53'),
(2, 1, 2, '123', NULL, '2025-12-13 07:19:43'),
(3, 1, 2, '123', NULL, '2025-12-13 07:26:10'),
(4, 1, 2, 'hello', NULL, '2025-12-13 07:41:29'),
(5, 5, 2, '支持楼主', NULL, '2025-12-14 07:46:09'),
(6, 5, 2, '支持\n必火', NULL, '2025-12-14 07:46:26'),
(7, 5, 3, '支持', NULL, '2025-12-14 07:47:32'),
(8, 9, 2, 'good', NULL, '2025-12-18 10:17:05'),
(9, 9, 2, 'hello', NULL, '2025-12-18 15:47:41'),
(10, 9, 2, 'hello', NULL, '2025-12-18 15:48:18'),
(11, 13, 1, '神女大人是对的', NULL, '2025-12-18 16:08:47'),
(20, 12, 1, 'Yes', NULL, '2025-12-19 02:33:35'),
(21, 20, 1, '确实如此', NULL, '2025-12-19 02:55:52'),
(22, 20, 2, 'hello', NULL, '2025-12-19 03:48:39'),
(24, 20, 2, 'hello', NULL, '2025-12-19 07:18:40');

-- --------------------------------------------------------

--
-- 表的结构 `post_likes`
--

CREATE TABLE `post_likes` (
  `id` int(11) NOT NULL,
  `post_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `post_likes`
--

INSERT INTO `post_likes` (`id`, `post_id`, `user_id`, `created_at`) VALUES
(5, 1, 2, '2025-12-13 07:41:03'),
(12, 6, 3, '2025-12-14 07:47:22'),
(13, 6, 2, '2025-12-15 06:42:05'),
(14, 5, 2, '2025-12-15 06:42:07'),
(15, 4, 2, '2025-12-15 06:42:08'),
(16, 2, 2, '2025-12-15 06:42:09'),
(17, 6, 1, '2025-12-16 18:30:39'),
(18, 5, 1, '2025-12-17 04:57:34'),
(19, 9, 2, '2025-12-18 10:34:00'),
(21, 7, 2, '2025-12-18 15:47:25'),
(24, 11, 2, '2025-12-18 15:47:32'),
(26, 11, 1, '2025-12-18 16:06:12'),
(27, 13, 1, '2025-12-18 16:08:36'),
(30, 12, 2, '2025-12-18 16:36:44'),
(32, 12, 1, '2025-12-19 02:55:40'),
(33, 20, 1, '2025-12-19 02:56:07'),
(35, 18, 2, '2025-12-19 03:48:03'),
(38, 23, 2, '2025-12-19 06:59:02'),
(39, 20, 2, '2025-12-19 07:18:35');

-- --------------------------------------------------------

--
-- 表的结构 `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `display_name` varchar(100) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `avatar_url` varchar(255) DEFAULT NULL,
  `is_online` tinyint(1) DEFAULT 0,
  `last_seen` timestamp NOT NULL DEFAULT current_timestamp(),
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `bio` text DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `users`
--

INSERT INTO `users` (`id`, `username`, `password_hash`, `display_name`, `email`, `avatar_url`, `is_online`, `last_seen`, `created_at`, `updated_at`, `bio`) VALUES
(1, 'Neka', '910e0084', 'Neka-t', '1090944612@qq.com', 'assets/images/avatars/Neka_1765948631137.jpeg', 0, '2026-01-02 04:02:51', '2025-11-26 05:10:05', '2026-01-02 04:02:51', 'I\'m going on...'),
(2, 'cico', '56760663', 'CICO', '28275000@163.com', 'assets/images/avatars/cico_1766067853267.png', 0, '2025-12-19 07:30:38', '2025-11-26 05:11:01', '2025-12-19 07:30:38', '我是西可，一名对计算机技术与系统设计保持长期兴趣的在校学生。\r\n当前关注即时通信系统、轻量化社交机制以及技术内容平台的设计与实现，注重从实际使用场景出发，将功能逻辑、系统结构与用户体验相结合，在实践中不断完善对系统工程与产品设计的理解。'),
(3, 'seawon', '56760663', 'gongcai', '28275010@163.com', 'assets/images/avatars/seawon_1766113351318.jpg', 0, '2025-12-19 04:29:59', '2025-11-27 07:59:19', '2025-12-19 04:29:59', '软件工程...'),
(4, 'moon', '56760663', '枕月', 'kexicico@gmail.com', 'assets/images/avatars/moon_1766076292461.jpg', 1, '2025-12-18 16:44:29', '2025-12-18 16:44:29', '2025-12-18 16:44:52', '喜欢西可'),
(5, 'xuxu', '56760663', 'Xusir', '28275001@163.com', NULL, 1, '2025-12-19 04:30:29', '2025-12-19 04:30:29', '2025-12-19 04:30:29', ''),
(6, 'ruojiu', '56759204', 'ruojiu', 'ruojiu295@gmail.com', NULL, 1, '2025-12-19 06:48:20', '2025-12-19 06:48:20', '2025-12-19 06:48:20', ''),
(7, 'xike', '56760663', '西可', '1206259@qq.com', NULL, 1, '2025-12-19 06:54:52', '2025-12-19 06:53:17', '2025-12-19 06:54:52', ''),
(8, 'Ruo', '56760663', 'Ruo', '2766876191@qq.com', NULL, 1, '2025-12-19 06:57:56', '2025-12-19 06:57:56', '2025-12-19 06:57:56', '');

--
-- 转储表的索引
--

--
-- 表的索引 `ai_chat_sessions`
--
ALTER TABLE `ai_chat_sessions`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_ai_sessions_user` (`user_id`),
  ADD KEY `idx_ai_sessions_updated` (`updated_at`);

--
-- 表的索引 `ai_messages`
--
ALTER TABLE `ai_messages`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `idx_ai_messages_session_created` (`session_id`,`created_at`);

--
-- 表的索引 `chat_sessions`
--
ALTER TABLE `chat_sessions`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_session` (`user1_id`,`user2_id`),
  ADD KEY `idx_user1_id` (`user1_id`),
  ADD KEY `idx_user2_id` (`user2_id`),
  ADD KEY `idx_updated_at` (`updated_at`);

--
-- 表的索引 `friend_relations`
--
ALTER TABLE `friend_relations`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_relation` (`user_id`,`target_user_id`,`relation_type`,`status`),
  ADD KEY `idx_user_id` (`user_id`),
  ADD KEY `idx_target_user_id` (`target_user_id`),
  ADD KEY `idx_relation_status` (`user_id`,`target_user_id`,`status`);

--
-- 表的索引 `groups`
--
ALTER TABLE `groups`
  ADD PRIMARY KEY (`group_id`);

--
-- 表的索引 `group_members`
--
ALTER TABLE `group_members`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_group_member` (`group_id`,`user_id`),
  ADD KEY `idx_user_id` (`user_id`);

--
-- 表的索引 `group_messages`
--
ALTER TABLE `group_messages`
  ADD PRIMARY KEY (`message_id`),
  ADD KEY `idx_group_id_sent_at` (`group_id`,`sent_at`);

--
-- 表的索引 `messages`
--
ALTER TABLE `messages`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_sender_receiver` (`sender_id`,`receiver_id`),
  ADD KEY `idx_receiver_sender` (`receiver_id`,`sender_id`),
  ADD KEY `idx_created_at` (`created_at`),
  ADD KEY `idx_is_post_created_at` (`created_at`);

--
-- 表的索引 `posts`
--
ALTER TABLE `posts`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_posts_author` (`author_id`),
  ADD KEY `idx_posts_created_at` (`created_at`);

--
-- 表的索引 `post_comments`
--
ALTER TABLE `post_comments`
  ADD PRIMARY KEY (`id`),
  ADD KEY `parent_id` (`parent_id`),
  ADD KEY `idx_post_comments_post` (`post_id`),
  ADD KEY `idx_post_comments_author` (`author_id`);

--
-- 表的索引 `post_likes`
--
ALTER TABLE `post_likes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_user_post_like` (`user_id`,`post_id`),
  ADD KEY `idx_post_likes_post` (`post_id`),
  ADD KEY `idx_post_likes_user` (`user_id`);

--
-- 表的索引 `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- 在导出的表使用AUTO_INCREMENT
--

--
-- 使用表AUTO_INCREMENT `ai_chat_sessions`
--
ALTER TABLE `ai_chat_sessions`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=26;

--
-- 使用表AUTO_INCREMENT `ai_messages`
--
ALTER TABLE `ai_messages`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=119;

--
-- 使用表AUTO_INCREMENT `chat_sessions`
--
ALTER TABLE `chat_sessions`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- 使用表AUTO_INCREMENT `friend_relations`
--
ALTER TABLE `friend_relations`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=50;

--
-- 使用表AUTO_INCREMENT `groups`
--
ALTER TABLE `groups`
  MODIFY `group_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- 使用表AUTO_INCREMENT `group_members`
--
ALTER TABLE `group_members`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=22;

--
-- 使用表AUTO_INCREMENT `group_messages`
--
ALTER TABLE `group_messages`
  MODIFY `message_id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=40;

--
-- 使用表AUTO_INCREMENT `messages`
--
ALTER TABLE `messages`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=214;

--
-- 使用表AUTO_INCREMENT `posts`
--
ALTER TABLE `posts`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

--
-- 使用表AUTO_INCREMENT `post_comments`
--
ALTER TABLE `post_comments`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=25;

--
-- 使用表AUTO_INCREMENT `post_likes`
--
ALTER TABLE `post_likes`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=40;

--
-- 使用表AUTO_INCREMENT `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- 限制导出的表
--

--
-- 限制表 `ai_chat_sessions`
--
ALTER TABLE `ai_chat_sessions`
  ADD CONSTRAINT `ai_chat_sessions_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- 限制表 `ai_messages`
--
ALTER TABLE `ai_messages`
  ADD CONSTRAINT `ai_messages_ibfk_1` FOREIGN KEY (`session_id`) REFERENCES `ai_chat_sessions` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `ai_messages_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- 限制表 `messages`
--
ALTER TABLE `messages`
  ADD CONSTRAINT `messages_ibfk_1` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `messages_ibfk_2` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- 限制表 `posts`
--
ALTER TABLE `posts`
  ADD CONSTRAINT `posts_ibfk_1` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- 限制表 `post_comments`
--
ALTER TABLE `post_comments`
  ADD CONSTRAINT `post_comments_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `post_comments_ibfk_2` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `post_comments_ibfk_3` FOREIGN KEY (`parent_id`) REFERENCES `post_comments` (`id`) ON DELETE CASCADE;

--
-- 限制表 `post_likes`
--
ALTER TABLE `post_likes`
  ADD CONSTRAINT `post_likes_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `post_likes_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
