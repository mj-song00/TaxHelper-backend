document.querySelectorAll('[data-question]').forEach((button) => {
    button.addEventListener('click', () => {
        const question = document.querySelector('#question');
        if (!question) return;
        question.value = button.dataset.question;
        question.focus();
    });
});

const chatForm = document.querySelector('[data-chat-form]');
if (chatForm) {
    const questionInput = chatForm.querySelector('#question');
    const submitButton = chatForm.querySelector('.send-button');
    const answerCard = document.querySelector('[data-answer-card]');
    const answerStatus = answerCard.querySelector('[data-answer-status]');
    const answerResult = answerCard.querySelector('[data-answer-result]');
    const answerQuestion = answerCard.querySelector('[data-answer-question]');
    const answerContent = answerCard.querySelector('[data-answer-content]');
    const sourceList = answerCard.querySelector('[data-source-list]');

    let submitAfterComposition = false;

    questionInput.addEventListener('keydown', (event) => {
        if (event.key !== 'Enter' || event.shiftKey) return;

        if (event.isComposing || event.keyCode === 229) {
            submitAfterComposition = true;
            return;
        }

        event.preventDefault();
        chatForm.requestSubmit();
    });

    questionInput.addEventListener('keyup', (event) => {
        if (event.key !== 'Enter' || event.shiftKey || !submitAfterComposition) return;

        submitAfterComposition = false;
        event.preventDefault();
        chatForm.requestSubmit();
    });

    chatForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        const question = questionInput.value.trim();
        if (!question || submitButton.disabled) return;

        answerCard.hidden = false;
        answerStatus.hidden = false;
        answerStatus.classList.remove('is-error');
        answerStatus.querySelector('span:last-child').textContent = '관련 법령과 판례를 찾고 있어요';
        answerResult.hidden = true;
        submitButton.disabled = true;
        answerCard.scrollIntoView({ behavior: 'smooth', block: 'nearest' });

        try {
            const response = await fetch('/api/ui/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ question, topK: 5 })
            });

            const payload = await response.json().catch(() => ({}));
            if (!response.ok) {
                throw new Error(payload.message || payload.detail || '답변을 가져오지 못했습니다.');
            }

            answerQuestion.textContent = payload.question || question;
            answerContent.textContent = payload.answer || '답변 내용이 없습니다.';
            renderSources(sourceList, payload.sources || []);
            answerStatus.hidden = true;
            answerResult.hidden = false;
        } catch (error) {
            answerStatus.classList.add('is-error');
            answerStatus.querySelector('.loading-dot').hidden = true;
            answerStatus.querySelector('span:last-child').textContent =
                error.message || 'AI 서비스에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요.';
        } finally {
            submitButton.disabled = false;
        }
    });
}

function renderSources(container, sources) {
    container.replaceChildren();
    if (!sources.length) return;

    const title = document.createElement('div');
    title.className = 'source-title';
    title.textContent = '답변 근거';
    container.appendChild(title);

    sources.slice(0, 5).forEach((source) => {
        const item = document.createElement('button');
        item.type = 'button';
        item.className = 'source-item';
        item.setAttribute('aria-expanded', 'false');

        const headingRow = document.createElement('span');
        headingRow.className = 'source-heading';
        const heading = document.createElement('strong');
        const sourceName = source.source_type === 'precedent' ? '판례' : (source.law_name || '법령');
        heading.textContent = `${sourceName} · ${source.title || source.case_number || '관련 근거'}`;
        const toggle = document.createElement('span');
        toggle.className = 'source-toggle';
        toggle.textContent = '전문 보기';
        headingRow.append(heading, toggle);

        const excerpt = document.createElement('p');
        excerpt.textContent = source.excerpt || '';

        const detail = document.createElement('span');
        detail.className = 'source-detail';
        detail.hidden = true;

        item.append(headingRow, excerpt, detail);
        item.addEventListener('click', async () => {
            const expanded = item.getAttribute('aria-expanded') === 'true';
            if (expanded) {
                item.setAttribute('aria-expanded', 'false');
                detail.hidden = true;
                excerpt.hidden = false;
                toggle.textContent = '전문 보기';
                return;
            }

            if (!item.dataset.loaded) {
                toggle.textContent = '불러오는 중…';
                item.disabled = true;
                try {
                    const sourceType = source.source_type === 'precedent' ? 'precedent' : 'law';
                    const response = await fetch(
                        `/api/ui/sources/${sourceType}/${encodeURIComponent(source.chunk_id)}`
                    );
                    const payload = await response.json().catch(() => ({}));
                    if (!response.ok) {
                        throw new Error(payload.message || '전문을 불러오지 못했습니다.');
                    }
                    detail.textContent = toPlainText(payload.content || source.excerpt || '내용이 없습니다.');
                    item.dataset.loaded = 'true';
                } catch (error) {
                    detail.textContent = error.message || '전문을 불러오지 못했습니다.';
                    detail.classList.add('is-error');
                } finally {
                    item.disabled = false;
                }
            }

            item.setAttribute('aria-expanded', 'true');
            excerpt.hidden = true;
            detail.hidden = false;
            toggle.textContent = '접기';
        });
        container.appendChild(item);
    });
}

function toPlainText(content) {
    return String(content)
        .replace(/<br\s*\/?\s*>/gi, '\n')
        .replace(/<[^>]*>/g, ' ')
        .replace(/[ \t]+/g, ' ')
        .replace(/\n{3,}/g, '\n\n')
        .trim();
}

const signupForm = document.querySelector('[data-signup-form]');
if (signupForm) {
    signupForm.addEventListener('submit', (event) => {
        const password = signupForm.querySelector('#password');
        const passwordConfirm = signupForm.querySelector('#passwordConfirm');
        const error = signupForm.querySelector('.field-error');

        if (password.value !== passwordConfirm.value) {
            event.preventDefault();
            error.textContent = '비밀번호가 서로 일치하지 않습니다.';
            passwordConfirm.focus();
        } else {
            error.textContent = '';
        }
    });
}
