/**
 * 测试辅助脚本
 * 在浏览器控制台中运行这些函数进行快速测试
 */

// 1. 测试语言切换功能
export function testLanguageSwitch() {
  const { i18n } = window;
  if (!i18n) {
    console.error('i18n not found. Make sure react-i18next is loaded.');
    return;
  }

  const currentLang = i18n.language;
  const newLang = currentLang === 'zh-CN' ? 'en-US' : 'zh-CN';
  
  console.log(`Current language: ${currentLang}`);
  console.log(`Switching to: ${newLang}`);
  
  i18n.changeLanguage(newLang);
  
  setTimeout(() => {
    console.log(`Language switched to: ${i18n.language}`);
    console.log('Please check if all text on the page has been updated.');
  }, 100);
}

// 2. 检查翻译完整性
export function checkTranslations() {
  const { i18n } = window;
  if (!i18n) {
    console.error('i18n not found.');
    return;
  }

  const zhKeys = Object.keys(i18n.store.data['zh-CN']?.translation || {});
  const enKeys = Object.keys(i18n.store.data['en-US']?.translation || {});
  
  const missingInEn = zhKeys.filter(k => !enKeys.includes(k));
  const missingInZh = enKeys.filter(k => !zhKeys.includes(k));
  
  console.log(`Total ZH keys: ${zhKeys.length}`);
  console.log(`Total EN keys: ${enKeys.length}`);
  
  if (missingInEn.length > 0) {
    console.warn(`Missing EN translations (${missingInEn.length}):`, missingInEn);
  } else {
    console.log('✅ All ZH keys have EN translations');
  }
  
  if (missingInZh.length > 0) {
    console.warn(`Missing ZH translations (${missingInZh.length}):`, missingInZh);
  } else {
    console.log('✅ All EN keys have ZH translations');
  }
  
  return { missingInEn, missingInZh };
}

// 3. 测试主题切换
export function testThemeSwitch() {
  const themes = ['default', 'yijing', 'tarot', 'zodiac', 'bazi', 'ai'];
  let currentIndex = 0;
  
  const switchTheme = () => {
    const theme = themes[currentIndex];
    document.documentElement.setAttribute('data-theme', theme);
    console.log(`Theme set to: ${theme}`);
    
    currentIndex = (currentIndex + 1) % themes.length;
    
    if (currentIndex !== 0) {
      setTimeout(switchTheme, 2000);
    } else {
      console.log('Theme test completed');
    }
  };
  
  switchTheme();
}

// 4. 检查硬编码的中文文本
export function checkHardcodedChinese() {
  const walker = document.createTreeWalker(
    document.body,
    NodeFilter.SHOW_TEXT,
    null,
    false
  );
  
  const chineseRegex = /[\u4e00-\u9fa5]/;
  const hardcodedTexts = [];
  let node;
  
  while (node = walker.nextNode()) {
    const text = node.textContent.trim();
    if (text && chineseRegex.test(text)) {
      // 排除已知的硬编码文本（如八卦符号等）
      if (!text.match(/^[☰☱☲☳☴☵☶☷🎴⏳💕✝️⚖️💼🧲☘️♈♉♊♋♌♍♎♏♐♑♒♓🔮]+$/)) {
        const parent = node.parentElement;
        if (parent && !parent.closest('[data-testid]')) {
          hardcodedTexts.push({
            text: text.substring(0, 50),
            element: parent.tagName,
            className: parent.className
          });
        }
      }
    }
  }
  
  if (hardcodedTexts.length > 0) {
    console.warn(`Found ${hardcodedTexts.length} potential hardcoded Chinese texts:`, hardcodedTexts);
  } else {
    console.log('✅ No hardcoded Chinese texts found');
  }
  
  return hardcodedTexts;
}

// 5. 测试所有页面路由
export function testRoutes() {
  const routes = [
    '/',
    '/yijing',
    '/tarot',
    '/zodiac',
    '/bazi',
    '/ai',
    '/login'
  ];
  
  console.log('Testing routes...');
  routes.forEach(route => {
    console.log(`Testing route: ${route}`);
    // 这里可以添加实际的导航测试
  });
}

// 6. 检查API端点
export function checkAPIEndpoints() {
  const endpoints = [
    '/api/yijing/quick',
    '/api/tarot/draw',
    '/api/zodiac/info',
    '/api/zodiac/horoscope',
    '/api/zodiac/compatibility',
    '/api/bazi/analyze',
    '/api/deepseek/interpret'
  ];
  
  console.log('Checking API endpoints...');
  endpoints.forEach(endpoint => {
    fetch(endpoint, { method: 'OPTIONS' })
      .then(() => console.log(`✅ ${endpoint} - Available`))
      .catch(() => console.warn(`❌ ${endpoint} - Not available`));
  });
}

// 7. 检查响应式断点
export function testResponsiveBreakpoints() {
  const breakpoints = [
    { name: 'Mobile', width: 375 },
    { name: 'Tablet', width: 768 },
    { name: 'Desktop', width: 1024 },
    { name: 'Large Desktop', width: 1920 }
  ];
  
  console.log('Testing responsive breakpoints...');
  breakpoints.forEach(({ name, width }) => {
    console.log(`Testing ${name} (${width}px)`);
    // 这里可以添加实际的视口测试
  });
}

// 8. 检查CSS变量
export function checkCSSVariables() {
  const themes = ['default', 'yijing', 'tarot', 'zodiac', 'bazi', 'ai'];
  const requiredVars = [
    '--color-primary',
    '--color-secondary',
    '--color-gradient-from',
    '--color-gradient-to'
  ];
  
  console.log('Checking CSS variables...');
  themes.forEach(theme => {
    document.documentElement.setAttribute('data-theme', theme);
    const computedStyle = getComputedStyle(document.documentElement);
    
    requiredVars.forEach(varName => {
      const value = computedStyle.getPropertyValue(varName);
      if (value) {
        console.log(`✅ ${theme}: ${varName} = ${value.trim()}`);
      } else {
        console.warn(`❌ ${theme}: ${varName} not found`);
      }
    });
  });
}

// 9. 性能测试
export function performanceTest() {
  if (!window.performance) {
    console.warn('Performance API not available');
    return;
  }
  
  const navigation = performance.getEntriesByType('navigation')[0];
  if (navigation) {
    console.log('Page Load Performance:');
    console.log(`- DNS: ${(navigation.domainLookupEnd - navigation.domainLookupStart).toFixed(2)}ms`);
    console.log(`- TCP: ${(navigation.connectEnd - navigation.connectStart).toFixed(2)}ms`);
    console.log(`- Request: ${(navigation.responseStart - navigation.requestStart).toFixed(2)}ms`);
    console.log(`- Response: ${(navigation.responseEnd - navigation.responseStart).toFixed(2)}ms`);
    console.log(`- DOM Processing: ${(navigation.domComplete - navigation.domInteractive).toFixed(2)}ms`);
    console.log(`- Total Load: ${(navigation.loadEventEnd - navigation.navigationStart).toFixed(2)}ms`);
  }
}

// 10. 检查错误边界
export function testErrorBoundary() {
  console.log('Testing ErrorBoundary...');
  // 这里可以添加实际的错误边界测试
  console.log('ErrorBoundary test completed');
}

// 导出所有测试函数
export const testHelpers = {
  testLanguageSwitch,
  checkTranslations,
  testThemeSwitch,
  checkHardcodedChinese,
  testRoutes,
  checkAPIEndpoints,
  testResponsiveBreakpoints,
  checkCSSVariables,
  performanceTest,
  testErrorBoundary
};

// 如果在浏览器环境中，将函数挂载到 window 对象
if (typeof window !== 'undefined') {
  window.testHelpers = testHelpers;
  console.log('✅ Test helpers loaded. Use window.testHelpers to access functions.');
  console.log('Available functions:', Object.keys(testHelpers));
}
